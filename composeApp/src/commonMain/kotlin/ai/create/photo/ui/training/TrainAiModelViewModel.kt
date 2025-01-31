package ai.create.photo.ui.training

import ai.create.photo.data.supabase.SessionViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

class TrainAiModelViewModel : SessionViewModel() {

    var uiState by mutableStateOf(TrainAiModelState())
        private set

    init {
        loadSession()
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        loadState()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun loadState() = viewModelScope.launch {
        Logger.i("loadState")
        val userId = userId ?: return@launch
        uiState = uiState.copy(isLoading = true)
        try {
            uiState = uiState.copy(isLoading = false)
        } catch (e: Exception) {
            Logger.e("Load state failed", e)
            uiState = uiState.copy(isLoading = false, loadingError = e)
        }
    }

    fun updateTrainingSteps(trainingSteps: Int) {
        uiState = uiState.copy(trainingSteps = trainingSteps)
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
