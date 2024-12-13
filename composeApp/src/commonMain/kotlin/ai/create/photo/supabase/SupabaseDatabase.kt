package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import ai.create.photo.supabase.model.UserFile
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.result.PostgrestResult

object SupabaseDatabase {

    private const val USER_FILES_TABLE = "user_files"

    suspend fun saveFile(userId: String, filePath: String): Result<PostgrestResult> = runCatching {
        val photoData = mapOf(
            "user_id" to userId,
            "file_path" to filePath
        )
        Logger.i("save file to db $filePath")
        supabase.from(USER_FILES_TABLE).upsert(photoData)
    }

    suspend fun getFiles(): Result<List<UserFile>> = runCatching {
        supabase
            .from(USER_FILES_TABLE)
            .select {
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<UserFile>()
            .also {
                Logger.i("getFiles result ${it.size}: ${it.joinToString()}")
            }
    }
}