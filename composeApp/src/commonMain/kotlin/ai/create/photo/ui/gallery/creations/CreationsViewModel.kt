package ai.create.photo.ui.gallery.creations

import ai.create.photo.data.supabase.SupabaseStorage
import ai.create.photo.data.supabase.database.UserFilesRepository
import ai.create.photo.data.supabase.database.UserGenerationsRepository
import ai.create.photo.data.supabase.model.GenerationsFilter
import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class CreationsViewModel : AuthViewModel() {

    var uiState by mutableStateOf(CreationsUiState())
        private set

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        if (uiState.photos.isNotEmpty()) {
            uiState = uiState.copy(isLoading = false)
        }
        if (userChanged) {
            uiState = CreationsUiState()
        }
        if (userChanged || uiState.photos.isEmpty()) {
            loadCreations()
        }
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun refreshCreations(silent: Boolean = false) = viewModelScope.launch {
        val userId = user?.id ?: return@launch
        if (uiState.isRefreshing) return@launch
        if (uiState.isLoadingNextPage && uiState.photos.isEmpty()) return@launch
        // user can generate 10 photos at once and last generate appears first
        val latestCreatedAt = uiState.photos.getOrNull(10)?.createdAt
        Logger.i("refreshCreations, silent=$silent")
        if (!silent) uiState = uiState.copy(isRefreshing = true)

        try {
            val generations = UserGenerationsRepository
                .getCreationsAfter(userId, latestCreatedAt, uiState.filter)
                .getOrThrow()

            val newPhotos = generations.map { CreationsUiState.Photo(it) }
            uiState = uiState.copy(
                photos = (newPhotos + uiState.photos).distinctBy { photo -> photo.id },
                isRefreshing = false,
                loadingError = null,
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isRefreshing = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("refreshCreations failed", e)
            uiState = uiState.copy(errorPopup = e)
        }
    }

    fun loadCreations() = viewModelScope.launch {
        val userId = user?.id ?: return@launch
        if (uiState.isLoadingNextPage) return@launch
        Logger.i("loadCreations")
        uiState = uiState.copy(isLoadingNextPage = true)
        try {
            val generations =
                UserGenerationsRepository.getCreations(
                    userId = userId,
                    page = uiState.page,
                    pageSize = 15,
                    filter = uiState.filter
                ).getOrThrow()
            val newPhotos = generations.map { CreationsUiState.Photo(it) }

            uiState = uiState.copy(
                isLoading = false,
                loadingError = null,
                scrollToTop = newPhotos.size > (uiState.photos.size),
                photos = (uiState.photos + newPhotos).distinctBy { photo -> photo.id },
                isLoadingNextPage = false,
                page = uiState.page + 1,
                pagingLimitReach = newPhotos.isEmpty(),
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isLoadingNextPage = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("loadCreations failed", e)
            uiState = uiState.copy(loadingError = e)
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
            uiState = uiState.copy(photos = photos)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("deleteGeneratedPhoto failed, $photo", e)
            uiState = uiState.copy(errorPopup = e)
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
            uiState = uiState.copy(photos = photos)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("makePublic failed, $photo", e)
            uiState = uiState.copy(errorPopup = e)
        }

    }

    fun downloadGeneratedPhoto(photo: CreationsUiState.Photo) = viewModelScope.launch {
        try {
            UserGenerationsRepository.downloadGeneratedPhoto(photo.id, photo.url)
        } catch (e: Exception) {
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("downloadGeneratedPhoto failed", e)
        }
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }

    fun toggleFilterDropDownMenu(show: Boolean) {
        uiState = uiState.copy(showFilterDropDownMenu = show)
    }

    fun filter(filter: GenerationsFilter) {
        if (uiState.filter == filter) return
        uiState =
            uiState.copy(
                filter = filter, page = 1, photos = emptyList(),
                isLoading = true, isLoadingNextPage = false
            )
        loadCreations()
    }
}
