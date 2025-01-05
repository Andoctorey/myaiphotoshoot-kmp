package ai.create.photo.ui.settings.login

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.Supabase.supabase
import ai.create.photo.data.supabase.SupabaseAuth
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.SupabaseStorage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

class LoginViewModel : SessionViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set
    var anonymousUserId: String? = null

    init {
        loadSession()
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated() {
        uiState = uiState.copy(isLoading = false)
        loadUser()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    private fun loadUser() {
        Logger.i("loadUser")
        val sessionStatus = supabase.auth.sessionStatus.value
        if (sessionStatus !is SessionStatus.Authenticated) return
        val user = sessionStatus.session.user
        val email = user?.email?.takeIf { user.confirmedAt != null }
        if (email == null) {
            anonymousUserId = user?.id
        }
        uiState = uiState.copy(email = email)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun onEmailChanged(email: String) {
        uiState = uiState.copy(emailToVerify = email, isInvalidEmail = false)
    }

    fun sendOtp() = viewModelScope.launch {
        Logger.i("sendOtp")
        uiState = uiState.copy(isInvalidEmail = false, isSendingOtp = true, enterOtp = false)
        if (!isValidEmail(uiState.emailToVerify)) {
            uiState = uiState.copy(isInvalidEmail = true, isSendingOtp = false)
            return@launch
        }

        try {
            SupabaseAuth.convertAnonymousUserToEmail(uiState.emailToVerify)
            SupabaseAuth.signInWithEmailOtp(uiState.emailToVerify)
            uiState = uiState.copy(isSendingOtp = false, enterOtp = true)
        } catch (e: Exception) {
            Logger.e("sendOtp failed", e)
            uiState = uiState.copy(isSendingOtp = false, errorPopup = e)
        }
    }

    fun verifyOtp() = viewModelScope.launch {
        Logger.i("verifyOtp")
        uiState = uiState.copy(isIncorrectOtp = false, isVerifyingOtp = true)
        if (!isValidOtpCode(uiState.otp)) {
            uiState = uiState.copy(isIncorrectOtp = true, isVerifyingOtp = false)
            return@launch
        }

        try {
            SupabaseAuth.verifyEmailOtp(uiState.emailToVerify, uiState.otp)
            anonymousUserId?.let { it ->
                anonymousUserId = null
            }
            uiState = uiState.copy(isVerifyingOtp = false)
            loadUser()
        } catch (e: Exception) {
            if (e is AuthRestException) {
                uiState = uiState.copy(isIncorrectOtp = true, isVerifyingOtp = false)
                return@launch
            }
            Logger.e("verifyOtp failed", e)
            uiState = uiState.copy(isVerifyingOtp = false, errorPopup = e)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(emailRegex)
    }

    private fun isValidOtpCode(otp: String): Boolean {
        val otpRegex = "^[0-9]{6}$".toRegex()
        return otp.matches(otpRegex)
    }

    fun onOtpChanged(otp: String) {
        uiState = uiState.copy(otp = otp, isIncorrectOtp = false)
    }

    fun logout() = viewModelScope.launch {
        Logger.i("logout")
        supabase.auth.signOut()
    }

    fun deleteAllData() = viewModelScope.launch {
        Logger.i("deleteAllData")
        try {
            uiState = uiState.copy(isLoading = true)
            SupabaseStorage.deleteUserFiles(userId)
            SupabaseFunction.deleteUser()
            logout()
        } catch (e: Exception) {
            Logger.e("deleteAllData failed", e)
            uiState = uiState.copy(isLoading = false, errorPopup = e)
        }
    }
}
