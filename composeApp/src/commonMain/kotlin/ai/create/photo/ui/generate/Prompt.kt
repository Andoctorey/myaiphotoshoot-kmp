package ai.create.photo.ui.generate

import kotlinx.serialization.Serializable

@Serializable
data class Prompt(
    val generationId: String,
    val text: String,
    val url: String? = null,
)