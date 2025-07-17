package ai.create.photo.data.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class BlogPost @OptIn(ExperimentalTime::class) constructor(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("photo_topics") val photoTopics: String? = null,
    @Serializable(with = InstantSerializer::class) @SerialName("created_at") val createdAt: Instant,
    @SerialName("meta_description") val metaDescription: String,
    @SerialName("featured_image_url") val featuredImageUrl: String? = null,
    @SerialName("section_photos") val sectionPhotos: String? = null,
    @SerialName("translations") val translations: Map<String, BlogTranslation>? = null
)

@Serializable
data class BlogTranslation(
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("meta_description") val metaDescription: String,
)

@Serializable
data class BlogListItem @OptIn(ExperimentalTime::class) constructor(
    @SerialName("id") val id: String,
    @Serializable(with = InstantSerializer::class) @SerialName("created_at") val createdAt: Instant,
    @SerialName("title") val title: String,
    @SerialName("meta_description") val metaDescription: String,
    @SerialName("section_photos") val sectionPhotos: Map<String, UserGeneration>,
)

@Serializable
data class BlogPostsResponse(
    @SerialName("posts") val posts: List<BlogListItem>,
    @SerialName("total") val total: Int,
    @SerialName("page") val page: Int,
    @SerialName("limit") val limit: Int,
    @SerialName("totalPages") val totalPages: Int
) 