package ai.create.photo.supabase.model

import kotlinx.serialization.SerialName

enum class TrainingStatus {
    @SerialName("idle")
    IDLE,
    @SerialName("processing")
    PROCESSING,
    @SerialName("succeeded")
    SUCCEEDED,
}