package ai.create.photo.ui.main

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.platform.updateGenerationProgress
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var uiState by mutableStateOf(MainUiState())
        private set


    fun generatePhoto(
        trainingId: String,
        prompt: String,
        parentGenerationId: String?,
        photosToGenerate: Int
    ) = viewModelScope.launch {
        updateGenerationsInProgress(uiState.generationsInProgress + photosToGenerate)
        try {
            repeat(photosToGenerate) {
                launch {
                    try {
                        SupabaseFunction.generatePhoto(trainingId, prompt, parentGenerationId)
                    } catch (e: Exception) {
                        currentCoroutineContext().ensureActive()
                        Logger.e("Generate photo failed", e)
                        uiState = uiState.copy(errorPopup = e)
                    } finally {
                        updateGenerationsInProgress(uiState.generationsInProgress - 1)
                    }
                }
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            Logger.e("Generate photo failed", e)
            updateGenerationsInProgress(uiState.generationsInProgress - 1)
        }
    }

    private fun updateGenerationsInProgress(progress: Int) {
        uiState = uiState.copy(generationsInProgress = progress)
        updateGenerationProgress(progress)
    }

    fun toggleOpenCreations(openCreations: Boolean) {
        uiState = uiState.copy(openCreations = openCreations)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun toggleOpenUploads(openUploads: Boolean) {
        uiState = uiState.copy(openUploads = openUploads)
    }

    fun toggleResetSettingTab(reset: Boolean) {
        uiState = uiState.copy(resetSettingTab = reset)
    }

}