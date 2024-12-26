package ai.create.photo.ui.settings.login

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.SupabaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

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

    fun sendOtp() = viewModelScope.launch {
        uiState = uiState.copy(isInvalidEmail = false, isSendingOtp = true)
        if (!isValidEmail(uiState.email)) {
            uiState = uiState.copy(isInvalidEmail = true, isSendingOtp = false)
            return@launch
        }

        try {
            SupabaseAuth.signInWithEmailOtp(uiState.email)
            uiState = uiState.copy(isSendingOtp = false, enterOtp = true)
        } catch (e: Exception) {
            Logger.e("Create model failed", e)
            uiState = uiState.copy(isSendingOtp = false, errorPopup = e)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(emailRegex)
    }
}
