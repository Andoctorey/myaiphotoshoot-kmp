package ai.create.photo.data.supabase.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class UserGeneration @OptIn(ExperimentalTime::class) constructor(
    @SerialName("id") val id: String,
    @Serializable(with = InstantSerializer::class) @SerialName("created_at") val createdAt: Instant,
    @SerialName("prompt") val prompt: String,
    @SerialName("public_url") val imageUrl: String,
    @SerialName("file_id") val fileId: String? = null,
    @SerialName("is_public") val isPublic: Boolean = false,
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val columns = serializer().descriptor.elementNames.toList()
    }
}

enum class GenerationsSort {
    NEW, POPULAR
}

enum class GenerationsFilter {
    ALL, PUBLIC
}