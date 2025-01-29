package ai.create.photo.ui.gallery

import ai.create.photo.data.supabase.model.UserGeneration
import androidx.compose.runtime.Immutable

@Immutable
data class GalleryUiState(
    val selectedTab: Int = 0,
    val addPhotosToPublicGallery: List<UserGeneration> = emptyList(),
    val removePhotoFromPublicGallery: List<String> = emptyList(),
)