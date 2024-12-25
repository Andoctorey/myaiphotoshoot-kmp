package ai.create.photo.data.supabase.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserGeneration(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("user_files") val file: UserFile,
    @SerialName("prompt") val prompt: String,
)