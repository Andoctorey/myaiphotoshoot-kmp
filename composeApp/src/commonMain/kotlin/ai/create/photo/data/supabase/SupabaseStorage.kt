package ai.create.photo.data.supabase

import co.touchlab.kermit.Logger
import io.github.jan.supabase.storage.FileUploadResponse
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

object SupabaseStorage {

    private const val BUCKET = "photos"
    const val UPLOADS = "uploads"
    const val TEMP = "temp"

    fun uploadPhoto(
        userId: String,
        fileName: String,
        file: ByteArray
    ): Flow<UploadStatus> {
        val filePath = "${userId}/$UPLOADS/${fileName}"
        Logger.i("uploadPhoto $filePath, size: ${file.size}")
        return Supabase.supabase.storage
            .from(BUCKET)
            .uploadAsFlow(
                path = filePath,
                data = file,
            ) {
                upsert = true
            }
    }

    suspend fun uploadPictureToPrompt(
        userId: String,
        file: ByteArray
    ): FileUploadResponse {
        val filePath = "${userId}/$TEMP/to_prompt.jpg"
        Logger.i("uploadPictureToPrompt $filePath, size: ${file.size}")
        return Supabase.supabase.storage
            .from(BUCKET)
            .upload(
                path = filePath,
                data = file,
            ) {
                upsert = true
            }
    }

    suspend fun createSignedUrl(userId: String, filePath: String) =
        Supabase.supabase.storage.from(BUCKET).createSignedUrl(
                path = "${userId}/$UPLOADS/${filePath}",
                expiresIn = 3650.days,
            )

    suspend fun createTempSignedUrl(userId: String, filePath: String) =
        Supabase.supabase.storage
            .from(BUCKET)
            .createSignedUrl(
                path = "${userId}/$TEMP/${filePath}",
                expiresIn = 5.minutes,
            )

    suspend fun deleteFile(path: String) {
        Logger.i("delete file from storage $path")
        retryWithBackoff {
            Supabase.supabase.storage.from(BUCKET).delete(path)
        }
    }

    suspend fun deleteFiles(paths: List<String>) {
        Logger.i("delete files from storage ${paths.joinToString()}")
        retryWithBackoff {
            Supabase.supabase.storage.from(BUCKET).delete(paths)
        }
    }

    suspend fun deleteUserFiles(uid: String) {
        Logger.i("deleteUserFiles $uid")
        val bucket = Supabase.supabase.storage.from(BUCKET)

        val userFiles = bucket.list("$uid/")
            .flatMap { folder ->
                bucket.list("$uid/${folder.name}/")
                    .map { file -> "$uid/${folder.name}/${file.name}" }
            }

        if (userFiles.isNotEmpty()) {
            bucket.delete(userFiles)
            Logger.i("Deleted files: ${userFiles.joinToString()}")
        } else {
            Logger.i("No files to delete for user $uid.")
        }
    }
}