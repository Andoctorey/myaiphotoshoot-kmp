package ai.create.photo.data.supabase.model

import kotlinx.serialization.SerialName

enum class TrainingStatus {
    @SerialName("analyzing_photos")
    ANALYZING_PHOTOS,

    @SerialName("select_photos")
    SELECT_PHOTOS,
    @SerialName("processing")
    PROCESSING,
    @SerialName("succeeded")
    SUCCEEDED,
}