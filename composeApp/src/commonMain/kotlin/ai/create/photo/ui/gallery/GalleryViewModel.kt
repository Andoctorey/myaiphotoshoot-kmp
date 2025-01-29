package ai.create.photo.ui.gallery

import ai.create.photo.data.supabase.model.UserGeneration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GalleryViewModel : ViewModel() {

    var uiState by mutableStateOf(GalleryUiState())
        private set

    fun selectTab(tab: Int) {
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
