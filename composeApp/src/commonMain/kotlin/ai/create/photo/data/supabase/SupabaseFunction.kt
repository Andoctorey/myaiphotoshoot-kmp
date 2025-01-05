package ai.create.photo.data.supabase

import co.touchlab.kermit.Logger
import io.github.jan.supabase.functions.functions

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
}