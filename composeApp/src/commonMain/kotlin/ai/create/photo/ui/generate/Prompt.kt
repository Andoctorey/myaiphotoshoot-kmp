package ai.create.photo.ui.generate

data class Prompt(
    val generationId: String,
    val text: String,
    val url: String? = null,
)