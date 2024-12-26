package ai.create.photo.ui.generate

import ai.create.photo.data.supabase.SessionViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GenerateViewModel : SessionViewModel() {

    var uiState by mutableStateOf(GenerateUiState())
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

    fun onPromptChanged(prompt: String) {
        uiState = uiState.copy(prompt = prompt)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

}
