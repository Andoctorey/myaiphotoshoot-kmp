package ai.create.photo.ui.add

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class AddUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val photoSet: Int = 1,
    val photosByPhotoSet: Map<Int, List<Photo>>? = null,
    val listState: LazyStaggeredGridState = LazyStaggeredGridState(),
    val scrollToTop: Boolean = false,
    val uploadProgress: Int = 0,
    val creatingModel: Boolean = false,

    val errorPopup: Throwable? = null,

    val showMenu: Boolean = false,
    val showUploadMorePhotosPopup: Boolean = false,
) {

    val displayingPhotos: List<Photo>?
        get() = photosByPhotoSet?.get(this@AddUiState.photoSet)

    val photoSets: List<Int>?
        get() = photosByPhotoSet?.keys?.toList()

    @Immutable
    data class Photo(
        val id: String,
        val path: String,
        val photoSet: Int,
        val url: String,
        val createdAt: Instant,
    )
}