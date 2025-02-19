package ai.create.photo.data.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Preferences(
    @SerialName("photos_to_generate") val photosToGenerate: Int? = null,
)