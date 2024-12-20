package ai.create.photo.ui.add

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class AddUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val photos: List<Photo>? = null,
    val listState: LazyStaggeredGridState = LazyStaggeredGridState(),

    val uploadProgress: Int = 0,
    val uploadError: Throwable? = null,

    val deleteError: Throwable? = null,

    val showMenu: Boolean = false,
    val showUploadMorePhotosPopup: Boolean = false,
) {

    @Immutable
    data class Photo(
        val id: String,
        val path: String,
        val url: String,
        val createdAt: Instant,
    )
}