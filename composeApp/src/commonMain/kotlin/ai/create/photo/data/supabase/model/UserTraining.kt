package ai.create.photo.data.supabase.model

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames

@Serializable
data class UserTraining(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("status") val status: TrainingStatus,
    @SerialName("trigger_word") val triggerWord: String,
    @SerialName("person_description") val personDescription: String?,
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val columns = serializer().descriptor.elementNames.toList()
    }
}