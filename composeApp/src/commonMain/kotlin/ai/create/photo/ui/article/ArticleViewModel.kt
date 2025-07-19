package ai.create.photo.ui.article

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.model.UserGeneration
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
            val topics = parsePhotoTopics(post.photoTopics, post.sectionPhotos)

            uiState = uiState.copy(
                isLoading = false,
                post = post,
                topics = topics
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

    private fun parsePhotoTopics(
        photoTopics: String,
        sectionPhotos: Map<String, UserGeneration>
    ): List<PhotoTopic> {
        if (photoTopics.isBlank()) return emptyList()

        return photoTopics.split('\n')
            .filter { it.trim().isNotEmpty() }
            .mapNotNull { line ->
                // Parse format: "1. **Section Name** - Brief explanation"
                val regex = Regex("""^\d+\.\s*\*\*(.*?)\*\*\s*-\s*(.*)""")
                val match = regex.find(line.trim())

                if (match != null) {
                    val title = match.groupValues[1].trim()
                    val description = match.groupValues[2].trim()
                    val photos = getPhotosForTopic(title, sectionPhotos)
                    PhotoTopic(title = title, description = description, photos = photos)
                } else {
                    // Fallback parsing for different formats
                    val parts = line.replace(Regex("""^\d+\.\s*"""), "").split(" - ", limit = 2)
                    if (parts.size >= 2) {
                        val title = parts[0].replace(Regex("""^\*\*|\*\*$"""), "").trim()
                        val description = parts[1].trim()
                        val photos = getPhotosForTopic(title, sectionPhotos)
                        PhotoTopic(title = title, description = description, photos = photos)
                    } else {
                        null
                    }
                }
            }
    }

    private fun getPhotosForTopic(
        topicTitle: String,
        sectionPhotos: Map<String, UserGeneration>
    ): List<UserGeneration> {
        // Photos are stored with keys like "Topic Name-0", "Topic Name-1", etc.
        return sectionPhotos.entries
            .filter { (key, _) ->
                // Extract the section name from the key (everything before the last dash)
                val lastDashIndex = key.lastIndexOf('-')
                if (lastDashIndex > 0) {
                    val sectionName = key.substring(0, lastDashIndex)
                    // Try exact match first, then normalized matching
                    sectionName.equals(topicTitle, ignoreCase = true) ||
                            normalizeSectionName(sectionName) == normalizeSectionName(topicTitle)
                } else {
                    false
                }
            }
            .map { it.value }
            .sortedBy { photo ->
                val key = sectionPhotos.entries.find { it.value.id == photo.id }?.key
                key?.substringAfterLast('-')?.toIntOrNull() ?: 0
            }
    }

    private fun normalizeSectionName(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
} 