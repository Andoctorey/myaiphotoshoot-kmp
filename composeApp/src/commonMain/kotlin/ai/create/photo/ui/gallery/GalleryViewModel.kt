package ai.create.photo.ui.gallery

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.database.UserFilesRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
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
        uiState = uiState.copy(isLoading = true)
        try {
            val files = UserFilesRepository.getOutputPhotos(userId).getOrThrow()
            uiState = uiState.copy(
                isLoading = false,
                loadingError = null,
                photos = files.map { file ->
                    GalleryUiState.Photo(
                        id = file.id,
                        createdAt = file.createdAt,
                        name = file.fileName,
                        photoSet = file.photoSet,
                        url = file.signedUrl,
                    )
                }
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, loadingError = e)
        }
    }

}
