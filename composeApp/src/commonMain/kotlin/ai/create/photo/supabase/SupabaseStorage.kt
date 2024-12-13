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
    private const val PHOTOS_FOLDER = "folder1"

    suspend fun uploadPhoto(userId: String, file: PlatformFile): Flow<UploadStatus> {
        val filePath = "${userId}/$PHOTOS_FOLDER/${file.name}"
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

    suspend fun createSignedUrl(userId: String, filePath: String) = supabase.storage
        .from(BUCKET)
        .createSignedUrl(
            path = "${userId}/$PHOTOS_FOLDER/${filePath}",
            expiresIn = 3650.days,
        )

    suspend fun deleteFile(path: String) {
        Logger.i("delete file from storage $path")
        supabase.storage.from(BUCKET).delete(path)
    }
}