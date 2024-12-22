package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.days

object SupabaseStorage {

    private const val BUCKET = "photos"

    suspend fun uploadPhoto(
        userId: String,
        photoSet: Int,
        file: PlatformFile
    ): Flow<UploadStatus> {
        val filePath = "${userId}/$photoSet/${file.name}"
        Logger.i("uploadPhoto $filePath, size: ${file.getSize()}")
        return supabase.storage
            .from(BUCKET)
            .uploadAsFlow(
                path = filePath,
                data = file.readBytes()
            ) {
                upsert = true
            }
    }

    suspend fun createSignedUrl(userId: String, photoSet: Int, filePath: String) = supabase.storage
        .from(BUCKET)
        .createSignedUrl(
            path = "${userId}/$photoSet/${filePath}",
            expiresIn = 3650.days,
        )

    suspend fun deleteFile(path: String) {
        Logger.i("delete file from storage $path")
        supabase.storage.from(BUCKET).delete(path)
    }

    suspend fun deletePhotoSet(userId: String, photoSet: Int) {
        Logger.i("delete photoSet from storage $photoSet")
        supabase.storage.from(BUCKET).delete("${userId}/$photoSet")
    }
}