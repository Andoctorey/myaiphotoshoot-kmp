package ai.create.photo.ui.generate

import ai.create.photo.data.MemoryStore
import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.SupabaseFunction
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
        uiState = uiState.copy(isLoading = false)
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun onPromptChanged(prompt: String) {
        uiState = uiState.copy(prompt = prompt)
    }

    fun generatePhoto() = viewModelScope.launch {
        val trainingId = MemoryStore.trainingId ?: return@launch
        uiState = uiState.copy(isGenerating = true)
        try {
            SupabaseFunction.generatePhoto(trainingId, uiState.prompt)
            uiState = uiState.copy(isGenerating = false)
        } catch (e: Exception) {
            Logger.e("Generate photo failed", e)
            uiState = uiState.copy(isGenerating = false, errorPopup = e)
        }
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

}
