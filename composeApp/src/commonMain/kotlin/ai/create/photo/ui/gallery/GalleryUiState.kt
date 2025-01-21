package ai.create.photo.ui.gallery

import androidx.compose.runtime.Immutable

@Immutable
data class GalleryUiState(
    val selectedTab: Int = 1,
)