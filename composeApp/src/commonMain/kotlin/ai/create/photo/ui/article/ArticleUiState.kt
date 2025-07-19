package ai.create.photo.ui.article

import ai.create.photo.data.supabase.model.UserGeneration
import androidx.compose.runtime.Immutable

@Immutable
data class ArticleUiState(
    val isLoading: Boolean = true,
    val loadingError: Throwable? = null,

    val title: String = "",
    val topics: List<PhotoTopic> = emptyList(),
)

@Immutable
data class PhotoTopic(
    val title: String,
    val description: String,
    val photos: List<UserGeneration> = emptyList()
) 