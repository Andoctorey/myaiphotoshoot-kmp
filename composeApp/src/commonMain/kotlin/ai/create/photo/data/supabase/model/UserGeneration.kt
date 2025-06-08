package ai.create.photo.data.supabase.model

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames

@Serializable
data class UserGeneration(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("prompt") val prompt: String,
    @SerialName("public_url") val imageUrl: String,
    @SerialName("file_id") val fileId: String?,
    @SerialName("is_public") val isPublic: Boolean,
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