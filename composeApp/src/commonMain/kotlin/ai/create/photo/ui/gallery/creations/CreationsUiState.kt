package ai.create.photo.ui.gallery.creations

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class CreationsUiState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val photos: List<Photo> = emptyList(),
    val page: Int = 1,
    val isLoadingNextPage: Boolean = false,
    val pagingLimitReach: Boolean = false,

    val listState: LazyStaggeredGridState = LazyStaggeredGridState(),
    val scrollToTop: Boolean = false,

    val errorPopup: Throwable? = null,
) {
    @Immutable
    data class Photo(
        val id: String,
        val createdAt: Instant,
        val url: String,
        val prompt: String,
        val fileId: String?,
        val isPublic: Boolean,
    )
}