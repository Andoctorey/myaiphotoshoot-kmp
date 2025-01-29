package ai.create.photo.ui.gallery.creations

import ai.create.photo.data.supabase.model.UserGeneration
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class CreationsUiState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val photos: List<Photo> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val page: Int = 1,
    val pagingLimitReach: Boolean = false,

    val listState: LazyGridState = LazyGridState(),
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
    ) {

        constructor(it: UserGeneration) : this(
            id = it.id,
            createdAt = it.createdAt,
            url = it.imageUrl,
            prompt = it.prompt,
            fileId = it.fileId,
            isPublic = it.isPublic
        )
    }
}

fun CreationsUiState.Photo.toUserGeneration() = UserGeneration(
    id = id,
    createdAt = createdAt,
    prompt = prompt,
    imageUrl = url,
    fileId = fileId,
    isPublic = isPublic
)