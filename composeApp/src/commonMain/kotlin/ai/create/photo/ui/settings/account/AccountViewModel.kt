package ai.create.photo.ui.settings.account

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.SupabaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.exception.AuthRestException
import kotlinx.coroutines.launch

class AccountViewModel : SessionViewModel() {

    var uiState by mutableStateOf(AccountUiState())
        private set

    init {
        loadSession()
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated() {
        uiState = uiState.copy(isLoading = false)
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun onEmailChanged(email: String) {
        uiState = uiState.copy(email = email, isInvalidEmail = false)
    }

    fun sendOtp() = viewModelScope.launch {
        uiState = uiState.copy(isInvalidEmail = false, isSendingOtp = true, enterOtp = false)
        if (!isValidEmail(uiState.email)) {
            uiState = uiState.copy(isInvalidEmail = true, isSendingOtp = false)
            return@launch
        }

        try {
            SupabaseAuth.convertAnonymousUserToEmail(uiState.email)
            SupabaseAuth.signInWithEmailOtp(uiState.email)
            uiState = uiState.copy(isSendingOtp = false, enterOtp = true)
        } catch (e: Exception) {
            Logger.e("sendOtp failed", e)
            uiState = uiState.copy(isSendingOtp = false, errorPopup = e)
        }
    }

    fun verifyOtp() = viewModelScope.launch {
        uiState = uiState.copy(isIncorrectOtp = false, isVerifyingOtp = true)
        if (!isValidOtpCode(uiState.otp)) {
            uiState = uiState.copy(isIncorrectOtp = true, isVerifyingOtp = false)
            return@launch
        }

        try {
            SupabaseAuth.verifyEmailOtp(uiState.email, uiState.otp)
            uiState = uiState.copy(isVerifyingOtp = false, isVerified = true)
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
}
