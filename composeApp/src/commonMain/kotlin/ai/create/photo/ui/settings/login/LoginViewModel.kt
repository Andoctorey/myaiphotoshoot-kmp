package ai.create.photo.ui.settings.login

import ai.create.photo.data.supabase.SessionViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LoginViewModel : SessionViewModel() {

    var uiState by mutableStateOf(LoginUiState())
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

    fun sendOtp() {
        uiState = uiState.copy(isInvalidEmail = false, isSendingOtp = true)
        if (!isValidEmail(uiState.email)) {
            uiState = uiState.copy(isInvalidEmail = true, isSendingOtp = false)
            return
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(emailRegex)
    }
}
