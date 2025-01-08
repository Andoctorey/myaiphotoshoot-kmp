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
                onRefreshAiVisionPrompt()
            }
            uiState =
                uiState.copy(
                    isLoading = false,
                    originalAiVisionPrompt = training?.personDescription ?: "",
                    aiVisionPrompt = training?.personDescription ?: ""
                )
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

    fun prepareToGenerate(onGenerate: (String) -> Unit) = viewModelScope.launch {
        val oldDescription = uiState.originalAiVisionPrompt
        val newDescription = uiState.aiVisionPrompt
        if (oldDescription == newDescription) {
            onGenerate(uiState.userPrompt)
            return@launch
        }

        try {
            UserTrainingsRepository.updatePersonDescription(
                MemoryStore.trainingId!!,
                newDescription
            )
                .getOrThrow()
            uiState = uiState.copy(originalAiVisionPrompt = newDescription)
            onGenerate(uiState.userPrompt)
        } catch (e: Exception) {
            Logger.e("Update description failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun onRefreshAiVisionPrompt() = viewModelScope.launch {
        uiState = uiState.copy(isLoadingAiVisionPrompt = true, aiVisionPrompt = " ")
        try {
            val trainingId = MemoryStore.trainingId!!
            SupabaseFunction.generatePersonDescription(trainingId)
            val training = UserTrainingsRepository.getTraining(trainingId).getOrThrow()
            uiState =
                uiState.copy(
                    isLoadingAiVisionPrompt = false,
                    originalAiVisionPrompt = training?.personDescription ?: "",
                    aiVisionPrompt = training?.personDescription ?: ""
                )

        } catch (e: Exception) {
            Logger.e("onRefreshAiVisionPrompt failed", e)
            uiState = uiState.copy(isLoadingAiVisionPrompt = false, errorPopup = e)
        }
    }

    fun surpriseMe() = viewModelScope.launch() {
        uiState = uiState.copy(isLoadingSurpriseMe = true)
        try {
            val prompt = SupabaseFunction.surpriseMe()
            uiState = uiState.copy(userPrompt = prompt, isLoadingSurpriseMe = false)
        } catch (e: Exception) {
            Logger.e("surpriseMe failed", e)
            uiState = uiState.copy(isLoadingSurpriseMe = false, errorPopup = e)
        }
    }

}
