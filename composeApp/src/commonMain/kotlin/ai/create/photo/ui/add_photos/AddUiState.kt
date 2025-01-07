package ai.create.photo.ui.add_photos

import ai.create.photo.data.supabase.model.AnalysisStatus
import ai.create.photo.data.supabase.model.TrainingStatus
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class AddUiState(
    val isLoadingPhotos: Boolean = false,
    val loadingError: Throwable? = null,
    val photoSet: Int = 1,
    val photosByPhotoSet: Map<Int, List<Photo>>? = null,
    val listState: LazyStaggeredGridState = LazyStaggeredGridState(),
    val scrollToTop: Boolean = false,
    val uploadProgress: Int = 0,

    val isLoadingTraining: Boolean = false,
    val trainingStatus: TrainingStatus? = null,

    val errorPopup: Throwable? = null,

    val showMenu: Boolean = false,
    val showUploadMorePhotosPopup: Boolean = false,
    val showCreatingModelPopup: Boolean = false,
    val openedCreatePhotosScreen: Boolean = false,
) {

    val displayingPhotos: List<Photo>?
        get() = photosByPhotoSet?.get(this@AddUiState.photoSet) ?: emptyList()

    val photoSets: List<Int>?
        get() = photosByPhotoSet?.keys?.toList()

    @Immutable
    data class Photo(
        val id: String,
        val name: String,
        val photoSet: Int,
        val url: String,
        val createdAt: Instant,
        val analysis: String?,
        val analysisStatus: AnalysisStatus?,
    ) {
        val isApproved: Boolean
            get() = analysis != null && analysisStatus == AnalysisStatus.APPROVED
    }
}