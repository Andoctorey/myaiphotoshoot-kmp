package ai.create.photo.ui.settings.balance

import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BalanceViewModel : AuthViewModel() {

    var uiState by mutableStateOf(BalanceUiState())
        private set

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        uiState = uiState.copy(isLoading = false)
        loadUser()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun onPromoCodeChanged(promoCode: String) {
        uiState = uiState.copy(promoCode = promoCode)
    }
}
