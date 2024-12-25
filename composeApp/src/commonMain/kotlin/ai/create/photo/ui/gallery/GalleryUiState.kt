package ai.create.photo.ui.gallery

import androidx.compose.runtime.Immutable

@Immutable
data class GalleryUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
)