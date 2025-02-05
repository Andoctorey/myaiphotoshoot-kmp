package ai.create.photo.ui.training

import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TrainAiModelViewModel : AuthViewModel() {

    var uiState by mutableStateOf(TrainAiModelState())
        private set

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        uiState = uiState.copy(isLoading = false)
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun showPhotosRequiredPopup(photosRequired: Int, photosTaken: Int) {
        uiState = uiState.copy(
            photosRequired = photosRequired,
            photosTaken = photosTaken,
            showPhotosRequiredPopup = true
        )
    }

    fun hidePhotosRequiredPopup() {
        uiState = uiState.copy(showPhotosRequiredPopup = false)
    }

}
