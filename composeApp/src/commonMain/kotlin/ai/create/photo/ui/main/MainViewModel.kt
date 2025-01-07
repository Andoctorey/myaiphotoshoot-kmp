package ai.create.photo.ui.main

import ai.create.photo.data.MemoryStore
import ai.create.photo.data.supabase.SupabaseFunction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var uiState by mutableStateOf(MainUiState())
        private set


    fun generatePhoto(prompt: String) = viewModelScope.launch {
        val trainingId = MemoryStore.trainingId ?: return@launch
        uiState = uiState.copy(generationsInProgress = uiState.generationsInProgress + 1)
        try {
            SupabaseFunction.generatePhoto(trainingId, prompt)
            uiState = uiState.copy(generationsInProgress = uiState.generationsInProgress - 1)
        } catch (e: Exception) {
            Logger.e("Generate photo failed", e)
            uiState = uiState.copy(
                generationsInProgress = uiState.generationsInProgress - 1,
                errorPopup = e
            )
        }
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun setGenerateScreenOpened() {
        uiState = uiState.copy(generateScreenOpened = true)
    }

}