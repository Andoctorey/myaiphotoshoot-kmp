package ai.create.photo.ui.gallery.creations

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class CreationsUiState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val photos: List<Photo>? = null,

    val listState: LazyStaggeredGridState = LazyStaggeredGridState(),
    val scrollToTop: Boolean = false,
) {
    @Immutable
    data class Photo(
        val id: String,
        val createdAt: Instant,
        val name: String,
        val photoSet: Int,
        val url: String,
        val prompt: String,
    )
}