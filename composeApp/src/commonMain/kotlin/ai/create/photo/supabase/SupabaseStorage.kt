package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import ai.create.photo.supabase.SupabaseAuth.userId
import co.touchlab.kermit.Logger
import io.github.jan.supabase.storage.SignedUrl
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.minutes

object SupabaseStorage {

    private const val BUCKET = "photos"
    private const val PHOTOS_FOLDER = "folder1"

    suspend fun uploadPhoto(file: PlatformFile): Flow<UploadStatus> {
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

    suspend fun getFileUrls(filePaths: List<String>) = runCatching {
        if (filePaths.isEmpty()) return@runCatching emptyList<SignedUrl>()
        supabase.storage
            .from(BUCKET)
            .createSignedUrls(
                expiresIn = 3.minutes,
                filePaths.map { "${userId}/$PHOTOS_FOLDER/${it}" }
            )
            .also { Logger.i("getFileUrls count ${it.size}") }
    }
}