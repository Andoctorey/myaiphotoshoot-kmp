package ai.create.photo.ui.article

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.platform.getLocale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

class ArticleViewModel : ViewModel() {

    var uiState by mutableStateOf(ArticleUiState())
        private set

    fun loadArticle(postId: String) = viewModelScope.launch {
        try {
            uiState = uiState.copy(
                isLoading = true,
                loadingError = null
            )

            val locale = getLocale()
            val post = SupabaseFunction.getBlogPost(id = postId, locale = locale)

            uiState = uiState.copy(
                isLoading = false,
                post = post
            )
        } catch (e: Exception) {
            Logger.e("loadBlogPost failed for postId: $postId", e)
            uiState = uiState.copy(
                isLoading = false,
                loadingError = e
            )
        }
    }

    fun hideErrorPopup() {
        uiState = uiState.copy(errorPopup = null)
    }
} 