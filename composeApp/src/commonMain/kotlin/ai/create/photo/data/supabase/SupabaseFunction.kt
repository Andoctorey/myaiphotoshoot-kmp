package ai.create.photo.data.supabase

import co.touchlab.kermit.Logger
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body

object SupabaseFunction {

    suspend fun createAiModel(photos: List<String>) {
        Logger.i("createAiModel for ${photos.joinToString()}")
        Supabase.supabase.functions.invoke(
            function = "training",
            body = mapOf("photos" to photos.joinToString())
        )
    }

    suspend fun generatePhoto(trainingId: String, prompt: String) {
        Logger.i("generatePhoto trainingId: $trainingId, prompt: $prompt")
        Supabase.supabase.functions.invoke(
            function = "generate",
            body = mapOf(
                "training_id" to trainingId,
                "prompt" to prompt
            )
        )
    }

    suspend fun deleteUser() {
        Logger.i("deleteUser")
        Supabase.supabase.functions.invoke(function = "delete-user")
    }

    suspend fun analyzePhoto(fileId: String) {
        Logger.i("analyzePhoto fileId: $fileId")
        Supabase.supabase.functions.invoke(
            function = "analyze-image",
            body = mapOf("file_id" to fileId)
        )
    }

    suspend fun generatePersonDescription(trainingId: String) {
        Logger.i("generatePersonDescription, trainingId: $trainingId")
        Supabase.supabase.functions.invoke(
            function = "generate-person-description",
            body = mapOf("training_id" to trainingId)
        )
    }

    suspend fun surpriseMe(): String {
        Logger.i("surpriseMe")
        val response = Supabase.supabase.functions.invoke(function = "surprise-me")
        return response.body<String>()
    }

    suspend fun enhancePrompt(prompt: String): String {
        Logger.i("enhancePrompt, prompt: $prompt")
        val response = Supabase.supabase.functions.invoke(
            function = "enhance-prompt",
            body = mapOf("prompt" to prompt)
        )
        return response.body<String>()
    }
}