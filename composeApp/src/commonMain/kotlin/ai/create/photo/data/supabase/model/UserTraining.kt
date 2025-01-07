package ai.create.photo.data.supabase.model

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
    @SerialName("person_description") val personDescription: String?,
)