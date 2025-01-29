package ai.create.photo.data.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserGeneration(
    @SerialName("id") val id: String,
    @SerialName("prompt") val prompt: String,
    @SerialName("public_url") val imageUrl: String,
    @SerialName("file_id") val fileId: String?,
    @SerialName("is_public") val isPublic: Boolean,
)