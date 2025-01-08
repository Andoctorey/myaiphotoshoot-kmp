package ai.create.photo.data.supabase

import co.touchlab.kermit.Logger
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body

object SupabaseFunction {

    suspend fun createAiModel(photoSet: Int) {
        Logger.i("createAiModel for $photoSet")
        Supabase.supabase.functions.invoke(
            function = "training",
            body = mapOf("photo_set" to photoSet)
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
}