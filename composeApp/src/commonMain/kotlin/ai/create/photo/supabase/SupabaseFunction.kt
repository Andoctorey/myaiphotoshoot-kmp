package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.functions.functions

object SupabaseFunction {

    suspend fun createAiModel(folder: String) {
        Logger.i("createAiModel for $folder")
        supabase.functions.invoke(
            function = "training",
            body = mapOf("folder" to folder)
        )
    }
}