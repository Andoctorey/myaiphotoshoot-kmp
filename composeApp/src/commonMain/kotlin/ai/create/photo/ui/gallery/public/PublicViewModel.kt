package ai.create.photo.ui.gallery.public

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.database.UserGenerationsRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

class PublicViewModel : SessionViewModel() {

    var uiState by mutableStateOf(PublicUiState())
        private set

    init {
        loadSession()
    }

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated() {
        loadPublicGallery()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun loadPublicGallery() = viewModelScope.launch {
        Logger.i("loadPublicGallery")
        if (uiState.isLoadingNextPage) return@launch
        uiState = uiState.copy(isLoadingNextPage = true)
        try {
            val generations =
                UserGenerationsRepository.getPublicGallery(uiState.page, 100).getOrThrow()
            val newPhotos = generations.map {
                PublicUiState.Photo(
                    id = it.id,
                    prompt = it.prompt,
                    url = it.imageUrl,
                    fileId = it.fileId,
                )
            }
            uiState = uiState.copy(
                loadingError = null,
                photos = uiState.photos + newPhotos,
                isLoadingNextPage = false,
                isLoading = false,
                page = uiState.page + 1,
                pagingLimitReach = newPhotos.isEmpty(),
            )
        } catch (e: Exception) {
            Logger.e("loadPublicGallery failed", e)
            uiState = uiState.copy(isLoadingNextPage = false, loadingError = e)
        }
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

}
