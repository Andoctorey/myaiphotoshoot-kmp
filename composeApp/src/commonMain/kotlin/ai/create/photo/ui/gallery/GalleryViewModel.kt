package ai.create.photo.ui.gallery

import ai.create.photo.data.supabase.database.ProfilesRepository
import ai.create.photo.data.supabase.model.UserGeneration
import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class GalleryViewModel : AuthViewModel() {

    var uiState by mutableStateOf(GalleryUiState())
        private set

    override fun onAuthInitializing() {
        // can ignore on this screen
    }

    override fun onAuthenticated(userChanged: Boolean) {
        if (userChanged || uiState.firstTrainingCompleted == null) {
            loadProfile()
        }
    }

    override fun onAuthError(error: Throwable) {
        // can ignore on this screen
    }

    fun loadProfile() = viewModelScope.launch {
        val userId = user?.id ?: return@launch

        try {
            val profile = ProfilesRepository.loadProfile(userId)
            val preferences = profile?.preferences
            val firstTrainingCompleted = preferences?.firstTrainingCompleted == true
            uiState = uiState.copy(
                firstTrainingCompleted = firstTrainingCompleted,
                selectedTab = if (firstTrainingCompleted) Tab.PUBLIC else Tab.UPLOADS,
            )
        } catch (e: Exception) {
            ensureActive()
            if (isAuthenticated) return@launch
            Logger.e("loadProfile failed", e)
            // can ignore on this screen
        }
    }

    fun selectTab(tab: Tab) {
        uiState = uiState.copy(selectedTab = tab)
    }

    fun addPhotoToPublicGallery(photo: UserGeneration) {
        val photos = uiState.addPhotosToPublicGallery.toMutableList()
        photos.add(photo)
        uiState = uiState.copy(addPhotosToPublicGallery = photos)
    }

    fun onAddedPhotoToPublicGallery() {
        uiState = uiState.copy(addPhotosToPublicGallery = emptyList())
    }

    fun removePhotoFromPublicGallery(photoId: String) {
        val photos = uiState.removePhotoFromPublicGallery.toMutableList()
        photos.add(photoId)
        uiState = uiState.copy(removePhotoFromPublicGallery = photos)
    }

    fun onRemovedPhotoFromPublicGallery() {
        uiState = uiState.copy(removePhotoFromPublicGallery = emptyList())
    }
}
