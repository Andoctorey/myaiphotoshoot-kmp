package ai.create.photo.ui.blog

import ai.create.photo.data.supabase.model.Blog
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Immutable

@Immutable
data class BlogUiState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val posts: List<Blog> = emptyList(),

    val isRefreshing: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val page: Int = 1,
    val pagingLimitReach: Boolean = false,

    val listState: LazyListState = LazyListState(),
    val scrollToTop: Boolean = false,

    val errorPopup: Throwable? = null,
)