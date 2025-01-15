package ai.create.photo.ui.gallery

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.database.UserGenerationsRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

class GalleryViewModel : SessionViewModel() {

    var uiState by mutableStateOf(GalleryUiState())
        private set

    init {
        loadSession()
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated() {
        loadGallery()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun loadGallery() = viewModelScope.launch {
        Logger.i("loadGallery")
        val userId = userId ?: return@launch
        uiState = uiState.copy(isLoading = true)
        try {
            val generations = UserGenerationsRepository.getGenerations(userId).getOrThrow()
            uiState = uiState.copy(
                isLoading = false,
                loadingError = null,
                scrollToTop = generations.size > (uiState.photos?.size ?: 0),
                photos = generations.map {
                    GalleryUiState.Photo(
                        id = it.id,
                        createdAt = it.createdAt,
                        name = it.file.fileName,
                        photoSet = it.file.photoSet,
                        prompt = it.prompt,
                        url = it.file.signedUrl,
                    )
                }
            )
        } catch (e: Exception) {
            Logger.e("Load gallery failed", e)
            uiState = uiState.copy(isLoading = false, loadingError = e)
        }
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

}
