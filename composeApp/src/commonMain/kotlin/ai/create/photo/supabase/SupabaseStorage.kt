package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.BUCKET
import ai.create.photo.supabase.Supabase.supabase
import ai.create.photo.supabase.SupabaseAuth.userId
import co.touchlab.kermit.Logger
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.Flow

object SupabaseStorage {

    suspend fun uploadPhoto(file: PlatformFile): Flow<UploadStatus> {
        val filePath = "${userId}/folder1/${file.name}"
        Logger.i("uploadPhoto $filePath, size: ${file.getSize()}")
        return supabase.storage
            .from(BUCKET)
            .uploadAsFlow(filePath, file.readBytes()) {
                upsert = true
            }
    }
}