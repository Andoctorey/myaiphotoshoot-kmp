package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import ai.create.photo.supabase.SupabaseAuth.userId
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult

object SupabaseDatabase {

    suspend fun saveFile(filePath: String): Result<PostgrestResult> = runCatching {
        val photoData = mapOf(
            "user_id" to userId,
            "file_path" to filePath
        )
        Logger.i("save file to db $filePath")
        supabase.from("user_files").upsert(photoData)
    }
}