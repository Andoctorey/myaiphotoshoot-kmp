package ai.create.photo.ui.gallery.public

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Immutable

@Immutable
data class PublicUiState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val photos: List<Photo> = emptyList(),
    val page: Int = 1,
    val isLoadingNextPage: Boolean = false,
    val pagingLimitReach: Boolean = false,

    val listState: LazyGridState = LazyGridState(),

    val errorPopup: Throwable? = null,
) {
    @Immutable
    data class Photo(
        val id: String,
        val url: String,
        val prompt: String,
        val fileId: String?,
    )
}