package ai.create.photo.ui.settings.balance

import ai.create.photo.ui.auth.SessionViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger

class BalanceViewModel : SessionViewModel() {

    var uiState by mutableStateOf(BalanceUiState())
        private set

    init {
        loadSession()
    }

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

    private fun loadUser() {
        Logger.i("loadUser")
    }
}
