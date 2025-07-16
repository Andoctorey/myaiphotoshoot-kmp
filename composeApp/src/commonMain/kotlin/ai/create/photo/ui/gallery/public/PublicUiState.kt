package ai.create.photo.ui.gallery.public

import ai.create.photo.data.supabase.model.GenerationsSort
import ai.create.photo.data.supabase.model.UserGeneration
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Immutable
data class PublicUiState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val sort: GenerationsSort = GenerationsSort.POPULAR,
    val photos: List<Photo> = emptyList(),
    val page: Int = 1,
    val isRefreshing: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val pagingLimitReach: Boolean = false,

    val listState: LazyGridState = LazyGridState(),

    val showTooltipPopup: Boolean = false,
    val showSortDropDownMenu: Boolean = false,
    val errorPopup: Throwable? = null,
) {
    @Immutable
    data class Photo @OptIn(ExperimentalTime::class) constructor(
        val id: String,
        val createdAt: Instant,
        val url: String,
        val prompt: String,
        val fileId: String?,
    ) {

        @OptIn(ExperimentalTime::class)
        constructor(it: UserGeneration) : this(
            id = it.id,
            createdAt = it.createdAt,
            url = it.imageUrl,
            prompt = it.prompt,
            fileId = it.fileId,
        )
    }
}