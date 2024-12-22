package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import ai.create.photo.supabase.model.UserFile
import co.touchlab.kermit.Logger
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.result.PostgrestResult

object SupabaseDatabase {

    private const val USER_FILES_TABLE = "user_files"

    suspend fun saveFile(
        userId: String,
        photoSet: Int,
        filePath: String
    ): Result<PostgrestResult> = runCatching {
        val photoData = mapOf(
            "user_id" to userId,
            "file_path" to filePath,
            "photo_set" to photoSet.toString(),
            "signed_url" to SupabaseStorage.createSignedUrl(userId, photoSet, filePath),
        )
        Logger.i("save file to db $filePath")
        supabase.from(USER_FILES_TABLE).upsert(photoData) {
            onConflict = "user_id, file_path, photo_set"
        }
    }

    suspend fun getFiles(userId: String): Result<List<UserFile>> = runCatching {
        supabase
            .from(USER_FILES_TABLE)
            .select {
                filter {
                    eq("user_id", userId)
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

    suspend fun deletePhotoSet(userId: String, photoSet: Int) {
        Logger.i("delete photoSet from db $photoSet")
        supabase.from(USER_FILES_TABLE).delete {
            filter {
                eq("user_id", userId)
                eq("photo_set", photoSet)
            }
        }
    }
}