package ai.create.photo.supabase.database

import ai.create.photo.supabase.Supabase.supabase
import ai.create.photo.supabase.SupabaseStorage
import ai.create.photo.supabase.model.UserFile
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.result.PostgrestResult

object UserFilesRepository {

    private const val USER_FILES_TABLE = "user_files"

    suspend fun saveFile(
        userId: String,
        photoSet: Int,
        fileName: String
    ): Result<PostgrestResult> = runCatching {
        val photoData = mapOf(
            "user_id" to userId,
            "file_name" to fileName,
            "photo_set" to photoSet.toString(),
            "type" to "input_image",
            "signed_url" to SupabaseStorage.createSignedUrl(userId, photoSet, fileName),
        )
        Logger.i("save file to db $fileName")
        supabase.from(USER_FILES_TABLE).upsert(photoData) {
            onConflict = "user_id, file_name, photo_set, type"
        }
    }

    suspend fun getFiles(userId: String): Result<List<UserFile>> = runCatching {
        supabase
            .from(USER_FILES_TABLE)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("type", "input_image")
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<UserFile>()
            .also {
                Logger.i("getFiles result ${it.size}: ${it.joinToString()}")
            }
    }

    suspend fun deleteFile(id: String) {
        Logger.i("delete file from db $id")
        supabase.from(USER_FILES_TABLE).delete {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun deleteFiles(ids: List<String>) {
        Logger.i("delete files from db ${ids.joinToString()}")
        supabase.from(USER_FILES_TABLE).delete {
            filter {
                isIn("id", ids)
            }
        }
    }
}