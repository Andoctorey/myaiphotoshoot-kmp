package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.functions.functions

object SupabaseFunction {

    suspend fun createAiModel(photoSet: Int) {
        Logger.i("createAiModel for $photoSet")
        supabase.functions.invoke(
            function = "training",
            body = mapOf("photo_set" to photoSet)
        )
    }

    suspend fun generatePhoto(prompt: String) {
        Logger.i("generatePhoto for $prompt")
        supabase.functions.invoke(
            function = "generate",
            body = mapOf(
//                "training_id" to photoSet,
                "prompt" to prompt
            )
        )
    }
}