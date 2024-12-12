package ai.create.photo.ui.create

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class CreateUiState(
    val isLoading: Boolean = false,
    val loadingError: String? = null,
    val photos: List<Photo>? = null,

    val uploadProgress: Int = 0,
    val uploadError: String? = null,
) {

    @Immutable
    data class Photo(
        val id: String,
        val url: String,
        val createdAt: Instant,
    )
}