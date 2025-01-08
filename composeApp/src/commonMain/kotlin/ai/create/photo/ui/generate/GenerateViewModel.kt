package ai.create.photo.ui.generate

import ai.create.photo.data.MemoryStore
import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.database.UserTrainingsRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

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
        loadTraining()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun loadTraining() = viewModelScope.launch {
        val trainingId = MemoryStore.trainingId
        if (trainingId == null) {
            uiState = uiState.copy(isLoading = false)
            return@launch
        }
        uiState = uiState.copy(isLoading = true, loadingError = null)

        try {
            var training = UserTrainingsRepository.getTraining(trainingId).getOrThrow()
            if (training?.personDescription.isNullOrEmpty()) {
                SupabaseFunction.generatePersonDescription(trainingId)
                training = UserTrainingsRepository.getTraining(trainingId).getOrThrow()
            }
            uiState =
                uiState.copy(isLoading = false, aiVisionPrompt = training?.personDescription ?: "")

        } catch (e: Exception) {
            Logger.e("Load training failed", e)
            uiState = uiState.copy(isLoading = false, loadingError = e)
        }
    }


    fun onAiVisionPromptChanged(prompt: String) {
        uiState = uiState.copy(aiVisionPrompt = prompt)
    }

    fun onUserPromptChanged(prompt: String) {
        uiState = uiState.copy(userPrompt = prompt)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun onExpand() {
        uiState = uiState.copy(expanded = !uiState.expanded)
    }

}
