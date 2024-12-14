package ai.create.photo.ui.create

import ai.create.photo.supabase.SessionViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CreateViewModel : SessionViewModel() {


    var uiState by mutableStateOf(CreateUiState())
        private set

    init {
        loadSession()
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated() {
    }

    override fun onError(error: Throwable) {
        uiState.copy(loadingError = error)
    }
}
