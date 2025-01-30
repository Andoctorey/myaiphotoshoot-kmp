package ai.create.photo.ui.gallery.creations

import ai.create.photo.data.supabase.SessionViewModel
import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.database.UserFilesRepository
import ai.create.photo.data.supabase.database.UserGenerationsRepository
import ai.create.photo.ui.compose.MakeToast
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
        if (uiState.photos.isNotEmpty()) {
            uiState = uiState.copy(isLoading = false)
        }
        loadCreations()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun refreshCreations() = viewModelScope.launch {
        val userId = userId ?: return@launch
        if (uiState.isRefreshing) return@launch
        val latestCreatedAt = uiState.photos.firstOrNull()?.createdAt ?: return@launch
        Logger.i("refreshCreations")
        uiState = uiState.copy(isRefreshing = true)

        try {
            val generations = UserGenerationsRepository
                .getCreationsAfter(userId, latestCreatedAt)
                .getOrThrow()

            val newPhotos = generations.map { CreationsUiState.Photo(it) }
            uiState = uiState.copy(
                photos = (newPhotos + uiState.photos).distinctBy { photo -> photo.id },
                isRefreshing = false,
                loadingError = null,
            )
        } catch (e: Exception) {
            Logger.e("refreshCreations failed", e)
            uiState = uiState.copy(isRefreshing = false, errorPopup = e)
        }
    }

    fun loadCreations() = viewModelScope.launch {
        Logger.i("loadCreations")
        val userId = userId ?: return@launch
        if (uiState.isLoadingNextPage) return@launch
        Logger.i("loadCreations")
        uiState = uiState.copy(isLoadingNextPage = true)
        try {
            val generations =
                UserGenerationsRepository.getCreations(userId, uiState.page, 15).getOrThrow()
            val newPhotos = generations.map { CreationsUiState.Photo(it) }

            uiState = uiState.copy(
                isLoading = false,
                loadingError = null,
                scrollToTop = newPhotos.size > (uiState.photos.size),
                photos = (uiState.photos + newPhotos).distinctBy { photo -> photo.id },
                isLoadingNextPage = false,
                isLoading = false,
                page = uiState.page + 1,
                pagingLimitReach = newPhotos.isEmpty(),
            )
        } catch (e: Exception) {
            Logger.e("loadCreations failed", e)
            uiState = uiState.copy(isLoadingNextPage = false, loadingError = e)
        }
    }

    fun delete(photo: CreationsUiState.Photo) = viewModelScope.launch {
        Logger.i("deleteGeneratedPhoto: $photo")
        val photos = uiState.photos
        val updatedPhotos = photos.filter { it.id != photo.id }
        uiState = uiState.copy(photos = updatedPhotos)
        try {
            if (photo.fileId != null) {
                val file = UserFilesRepository.getFile(photo.fileId)!!
                SupabaseStorage.deleteFile(file.fileName)
                UserGenerationsRepository.deleteGeneratedPhoto(photo.id)
                UserFilesRepository.deleteFile(photo.fileId)
            } else {
                UserGenerationsRepository.deleteGeneratedPhoto(photo.id)
            }
        } catch (e: Exception) {
            Logger.e("deleteGeneratedPhoto failed, $photo", e)
            uiState = uiState.copy(photos = photos, errorPopup = e)
        }
    }

    fun togglePublic(photo: CreationsUiState.Photo, onSuccess: () -> Unit) = viewModelScope.launch {
        Logger.i("togglePublic: $photo")
        val photos = uiState.photos
        val public = !photo.isPublic
        val updatedPhotos = photos.map {
            if (it.id == photo.id) {
                it.copy(isPublic = public)
            } else {
                it
            }
        }
        uiState = uiState.copy(photos = updatedPhotos)
        try {
            UserGenerationsRepository.setPublic(photo.id, public)
            onSuccess()
        } catch (e: Exception) {
            Logger.e("makePublic failed, $photo", e)
            uiState = uiState.copy(photos = photos, errorPopup = e)
        }

    }

    fun downloadGeneratedPhoto(photo: CreationsUiState.Photo) = viewModelScope.launch {
        try {
            UserGenerationsRepository.downloadGeneratedPhoto(photo.url)
        } catch (e: Exception) {
            Logger.i("Faild downloading - $e")
        }
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

}
