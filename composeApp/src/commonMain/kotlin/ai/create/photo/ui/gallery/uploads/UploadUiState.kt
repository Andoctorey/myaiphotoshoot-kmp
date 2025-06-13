package ai.create.photo.ui.gallery.uploads

import ai.create.photo.data.supabase.model.AnalysisStatus
import ai.create.photo.data.supabase.model.TrainingStatus
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class UploadUiState(
    val isLoadingPhotos: Boolean = false,
    val loadingError: Throwable? = null,

    val photos: List<Photo>? = null,
    val listState: LazyStaggeredGridState = LazyStaggeredGridState(),
    val scrollToTop: Boolean = false,

    val uploadProgress: Int = 0,

    val analyzingPhotos: Int = 0,
    val showAnalysisForAll: Boolean = false,

    val isLoadingTraining: Boolean = false,
    val trainingStatus: TrainingStatus? = null,

    val errorPopup: Throwable? = null,
    val showUploadMorePhotosPopup: Boolean = false,
    val showTrainingAiModelPopup: Boolean = false,

    val showDeleteSomePhotosPopup: Boolean = false,
    val deleteUnsuitablePhotosPopup: Boolean = false,
    val topUpErrorPopup: Throwable? = null,
) {

    @Immutable
    data class Photo(
        val id: String,
        val name: String,
        val url: String,
        val createdAt: Instant,
        val analysis: String?,
        val analysisStatus: AnalysisStatus?,
    )
}