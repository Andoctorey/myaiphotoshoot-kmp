package ai.create.photo.ui.create

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class CreateUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val photos: List<Photo>? = null,
    val listState: LazyStaggeredGridState = LazyStaggeredGridState(),

    val uploadProgress: Int = 0,
    val uploadError: Throwable? = null,
) {

    @Immutable
    data class Photo(
        val id: String,
        val url: String,
        val createdAt: Instant,
    )
}