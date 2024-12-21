package ai.create.photo.ui.add

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class AddUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val folder: String? = null,
    val photosByFolder: Map<String, List<Photo>>? = null,
    val listState: LazyStaggeredGridState = LazyStaggeredGridState(),
    val scrollToTop: Boolean = false,
    val uploadProgress: Int = 0,
    val createModelStatus: CreateModelStatus = CreateModelStatus.Idle,

    val errorPopup: Throwable? = null,

    val showMenu: Boolean = false,
    val showUploadMorePhotosPopup: Boolean = false,
) {

    val displayingPhotos: List<Photo>?
        get() = photosByFolder?.get(folder)

    val folders: List<String>?
        get() = photosByFolder?.keys?.toList()

    @Immutable
    data class Photo(
        val id: String,
        val path: String,
        val folder: String,
        val url: String,
        val createdAt: Instant,
    )
}