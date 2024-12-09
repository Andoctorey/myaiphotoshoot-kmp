package ai.create.photo.supabase

import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus.Authenticated
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object Supabase {

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://trzgfajvyjpvbqedyxug.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRyemdmYWp2eWpwdmJxZWR5eHVnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzM1NTA5MzAsImV4cCI6MjA0OTEyNjkzMH0.39Qdq2nTCuoIpAfc7L725MZA2ls3NegFy6zCjOTzW9M"
    ) {
        install(Auth)
        install(Postgrest)
        install(Auth)
        install(Storage)
        install(Realtime)
    }

    init {
        MainScope().launch {
            supabase.auth.signInAnonymously()
        }
    }

    val authStatus = supabase.auth.sessionStatus.onEach {
        Logger.i("Auth status: $it")
    }.stateIn(MainScope(), SharingStarted.Eagerly, null)

    val userId: String
        get() = (authStatus.value as? Authenticated)?.session?.user?.id
            ?: throw IllegalStateException("Please sign in")

    suspend fun uploadPhoto(file: PlatformFile): Flow<UploadStatus> {
        val filePath = "${userId}/${file.name}"
        Logger.i("uploadPhoto $filePath, size: ${file.getSize()}")
        return supabase.storage
            .from("photos")
            .uploadAsFlow(filePath, file.readBytes()) {
                upsert = true
            }
    }
}