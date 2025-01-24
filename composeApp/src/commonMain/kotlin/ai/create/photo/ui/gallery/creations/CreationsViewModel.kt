package ai.create.photo.ui.gallery.creations

import ai.create.photo.data.supabase.SessionViewModel
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
        uiState = uiState.copy(isLoading = false)
        loadCreations()
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun loadCreations() = viewModelScope.launch {
        Logger.i("loadCreations")
        val userId = userId ?: return@launch
        uiState = uiState.copy(isLoadingNextPage = true)
        try {
            val generations =
                UserGenerationsRepository.getGenerations(userId, uiState.page, 15).getOrThrow()
            val newPhotos = generations.map {
                CreationsUiState.Photo(
                    id = it.id,
                    createdAt = it.createdAt,
                    name = it.file.fileName,
                    prompt = it.prompt,
                    url = it.file.signedUrl,
                )
            }
            uiState = uiState.copy(
                loadingError = null,
                scrollToTop = generations.size > (uiState.photos.size),
                photos = uiState.photos + newPhotos,
                isLoadingNextPage = false,
                page = uiState.page + 1,
                pagingLimitReach = newPhotos.isEmpty(),
            )
        } catch (e: Exception) {
            Logger.e("loadCreations failed", e)
            uiState = uiState.copy(isLoadingNextPage = false, loadingError = e) //TODO
        }
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

}
