package ai.create.photo.data.supabase.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class UserTraining @OptIn(ExperimentalTime::class) constructor(
    @SerialName("id") val id: String,
    @Serializable(with = InstantSerializer::class) @SerialName("created_at") val createdAt: Instant,
    @SerialName("status") val status: TrainingStatus,
    @SerialName("person_description") val personDescription: String?,
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val columns = serializer().descriptor.elementNames.toList()
    }
}