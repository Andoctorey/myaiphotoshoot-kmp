package ai.create.photo.ui.settings.login

import ai.create.photo.data.supabase.Supabase.supabase
import ai.create.photo.data.supabase.SupabaseAuth
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class LoginViewModel : AuthViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set


    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        uiState = uiState.copy(
            isLoading = false,
            email = user?.email?.takeIf { !uiState.isSendingOtp && !uiState.enterOtp })
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun onEmailChanged(email: String) {
        uiState = uiState.copy(emailToVerify = email, isInvalidEmail = false)
    }

    fun sendOtp() = viewModelScope.launch {
        Logger.i("sendOtp")
        val email = uiState.emailToVerify.trim()
        uiState = uiState.copy(
            emailToVerify = email,
            isInvalidEmail = false,
            isSendingOtp = true,
            enterOtp = false,
        )
        if (!isValidEmail(email)) {
            uiState = uiState.copy(isInvalidEmail = true, isSendingOtp = false)
            return@launch
        }

        try {
            try {
                SupabaseAuth.convertAnonymousUserToEmail(email)
            } catch (e: AuthRestException) {
                Logger.i("convertAnonymousUserToEmail", e)
                try {
                    SupabaseFunction.deleteUser()
                } catch (e: Exception) {
                    ensureActive()
                    Logger.w("deleteUser failed", e)
                }
            }
            SupabaseAuth.signInWithEmailOtp(email)
            uiState = uiState.copy(isSendingOtp = false, enterOtp = true)
        } catch (e: Exception) {
            uiState = uiState.copy(isSendingOtp = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            if (e is AuthRestException && isInvalidEmailFormatError(e)) {
                Logger.w("sendOtp rejected due to invalid email format")
                uiState = uiState.copy(isInvalidEmail = true)
                return@launch
            }
            Logger.e("sendOtp failed", e)
            uiState = uiState.copy(errorPopup = e)
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
            loadUser()
            uiState = uiState.copy(
                isVerifyingOtp = false, otpVerified = true,
                otp = "", email = user?.email
            )
        } catch (e: Exception) {
            if (e is AuthRestException) {
                uiState = uiState.copy(isIncorrectOtp = true, isVerifyingOtp = false)
                return@launch
            }
            uiState = uiState.copy(isVerifyingOtp = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("verifyOtp failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        // Require a domain with at least one dot segment and no empty labels (e.g. reject `a@.com`).
        val emailRegex = "^[A-Za-z0-9+_.%-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$".toRegex()
        return email.matches(emailRegex)
    }

    private fun isInvalidEmailFormatError(error: AuthRestException): Boolean {
        val message = error.message.orEmpty().lowercase()
        return "validation_failed" in message && "invalid format" in message
    }

    private fun isValidOtpCode(otp: String): Boolean {
        val otpRegex = "^[0-9]{6}$".toRegex()
        return otp.matches(otpRegex)
    }

    fun onOtpChanged(otp: String) {
        uiState = uiState.copy(otp = otp, isIncorrectOtp = false)
    }

    fun logout() = viewModelScope.launch {
        performLogout()
    }

    fun deleteAllData() = viewModelScope.launch {
        val userId = user?.id ?: return@launch

        Logger.i("deleteAllData")
        try {
            uiState = uiState.copy(isLoading = true)
            SupabaseStorage.deleteUserFiles(userId)
            SupabaseFunction.deleteUser()
            val logoutSucceeded = performLogout()
            if (logoutSucceeded) {
                uiState = uiState.copy(dataDeletedPopup = true)
            } else {
                uiState = uiState.copy(isLoading = false)
            }
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("deleteAllData failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun hideDataDeletedPopup() {
        uiState = uiState.copy(dataDeletedPopup = false)
    }

    fun toggleConfirmDeletePopup(show: Boolean) {
        uiState = uiState.copy(confirmDeletedPopup = show)
    }

    fun resetOtpVerified() {
        uiState = uiState.copy(otpVerified = false)
    }

    private suspend fun performLogout(): Boolean {
        Logger.i("logout")

        val signOutFailed = try {
            supabase.auth.signOut(SignOutScope.LOCAL)
            false
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Logger.w("signOut failed, clearing local session", e)
            true
        }

        var clearSessionFailed = false
        if (signOutFailed) {
            try {
                supabase.auth.clearSession()
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                Logger.e("clearSession failed", e)
                clearSessionFailed = true
                if (isAuthenticated) uiState = uiState.copy(errorPopup = e)
            }
        }

        if (clearSessionFailed) return false
        uiState = LoginUiState()
        return true
    }
}
