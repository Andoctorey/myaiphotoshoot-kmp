package ai.create.photo.ui.gallery.creations

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.database.UserGenerationsRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

class CreationsViewModel : SessionViewModel() {

    var uiState by mutableStateOf(CreationsUiState())
        private set

    init {
        loadSession()
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated() {
        loadCreations()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun loadCreations() = viewModelScope.launch {
        Logger.i("loadCreations")
        val userId = userId ?: return@launch
        uiState = uiState.copy(isLoading = true)
        try {
            val generations = UserGenerationsRepository.getGenerations(userId).getOrThrow()
            uiState = uiState.copy(
                isLoading = false,
                loadingError = null,
                scrollToTop = generations.size > (uiState.photos?.size ?: 0),
                photos = generations.map {
                    CreationsUiState.Photo(
                        id = it.id,
                        createdAt = it.createdAt,
                        name = it.file.fileName,
                        prompt = it.prompt,
                        url = it.file.signedUrl,
                    )
                }
            )
        } catch (e: Exception) {
            Logger.e("loadCreations failed", e)
            uiState = uiState.copy(isLoading = false, loadingError = e)
        }
    }

    fun deleteGeneratedPhoto(photo: CreationsUiState.Photo) = viewModelScope.launch {
        val photos = uiState.photos ?: return@launch
        val updatedPhotos = photos.filter { it.id != photo.id }
        uiState = uiState.copy(photos = updatedPhotos)
        try {
            SupabaseStorage.deleteFile(photo.name)
            UserGenerationsRepository.deleteGeneratedPhoto(photo.id)
        } catch (e: Exception) {
            Logger.e("deleteGeneratedPhoto failed, $photo", e)
            uiState = uiState.copy(photos = photos, errorPopup = e)
        }
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

}
