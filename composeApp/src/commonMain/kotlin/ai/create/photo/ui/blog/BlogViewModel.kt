package ai.create.photo.ui.blog

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.platform.getLocale
import ai.create.photo.ui.auth.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class BlogsViewModel : AuthViewModel() {

    var uiState by mutableStateOf(BlogUiState())
        private set

    override fun onAuthInitializing() {
        uiState = uiState.copy(isLoading = true)
    }

    override fun onAuthenticated(userChanged: Boolean) {
        if (uiState.posts.isNotEmpty()) {
            uiState = uiState.copy(isLoading = false)
        }
        if (userChanged) {
            uiState = BlogUiState()
        }
        if (userChanged || uiState.posts.isEmpty()) {
            loadBlogPosts()
        }
    }

    override fun onAuthError(error: Throwable) {
        uiState = uiState.copy(loadingError = error)
    }

    fun loadBlogPosts(page: Int = 1, refresh: Boolean = false) = viewModelScope.launch {
        if (uiState.isLoadingNextPage) return@launch
        if (uiState.isRefreshing) return@launch

        try {
            uiState = uiState.copy(
                isLoadingNextPage = true,
                loadingError = null,
                isRefreshing = refresh
            )
            val locale = getLocale()
            val response = SupabaseFunction.getBlogPosts(page = page, limit = 10, locale = locale)
            uiState = uiState.copy(
                isLoading = false,
                isRefreshing = false,
                isLoadingNextPage = false,
                posts = ((if (refresh) emptyList() else uiState.posts)
                        + response.posts).distinctBy { post -> post.id },
                scrollToTop = response.posts.size > (uiState.posts.size),
                page = response.page,
                pagingLimitReach = response.page >= response.totalPages
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isLoadingNextPage = false)
            ensureActive()
            if (!isAuthenticated) return@launch
            Logger.e("loadBlogPosts failed", e)
            uiState = uiState.copy(loadingError = e)
        }
    }

    fun refresh() {
        loadBlogPosts(page = 1, refresh = true)
    }

    fun loadMorePosts() {
        if (!uiState.pagingLimitReach && !uiState.isLoading) {
            loadBlogPosts(page = uiState.page + 1)
        }
    }

    fun resetScrollToTop() {
        uiState = uiState.copy(scrollToTop = false)
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }
} 