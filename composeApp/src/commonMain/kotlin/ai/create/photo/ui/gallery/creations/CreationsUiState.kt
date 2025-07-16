package ai.create.photo.ui.gallery.creations

import ai.create.photo.data.supabase.model.GenerationsFilter
import ai.create.photo.data.supabase.model.UserGeneration
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Immutable
data class CreationsUiState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val filter: GenerationsFilter = GenerationsFilter.ALL,
    val photos: List<Photo> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val page: Int = 1,
    val pagingLimitReach: Boolean = false,

    val listState: LazyGridState = LazyGridState(),
    val scrollToTop: Boolean = false,

    val showFilterDropDownMenu: Boolean = false,
    val errorPopup: Throwable? = null,
) {
    @Immutable
    data class Photo @OptIn(ExperimentalTime::class) constructor(
        val id: String,
        val createdAt: Instant,
        val url: String,
        val prompt: String,
        val fileId: String?,
        val isPublic: Boolean,
    ) {

        @OptIn(ExperimentalTime::class)
        constructor(it: UserGeneration) : this(
            id = it.id,
            createdAt = it.createdAt,
            url = it.imageUrl,
            prompt = it.prompt,
            fileId = it.fileId,
            isPublic = it.isPublic,
        )
    }
}

@OptIn(ExperimentalTime::class)
fun CreationsUiState.Photo.toUserGeneration() = UserGeneration(
    id = id,
    createdAt = createdAt,
    prompt = prompt,
    imageUrl = url,
    fileId = fileId,
    isPublic = isPublic,
)