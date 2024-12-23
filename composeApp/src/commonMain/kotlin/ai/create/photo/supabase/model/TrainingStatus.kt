package ai.create.photo.supabase.model

import kotlinx.serialization.SerialName

enum class TrainingStatus {
    @SerialName("processing")
    PROCESSING,
    @SerialName("succeeded")
    SUCCEEDED,
}