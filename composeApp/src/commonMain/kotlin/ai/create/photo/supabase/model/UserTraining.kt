package ai.create.photo.supabase.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserTraining(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("photo_set") val photoSet: Int,
    @SerialName("status") val status: TrainingStatus,
    @SerialName("trigger_word") val triggerWord: String,
)