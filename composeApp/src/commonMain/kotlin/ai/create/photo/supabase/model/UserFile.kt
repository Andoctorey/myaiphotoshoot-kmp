package ai.create.photo.supabase.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserFile(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("photo_set") val photoSet: Int,
    @SerialName("file_name") val fileName: String,
    @SerialName("signed_url") val signedUrl: String,
)