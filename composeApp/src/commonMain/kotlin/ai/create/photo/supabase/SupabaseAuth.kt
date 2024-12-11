package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus.Authenticated
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object SupabaseAuth {

    init {
        MainScope().launch {

        }
    }

    val authStatus = supabase.auth.sessionStatus.onEach {
        Logger.i("Auth status: $it")
    }.stateIn(MainScope(), SharingStarted.Eagerly, null)

    val userId: String
        get() = (authStatus.value as? Authenticated)?.session?.user?.id
            ?: throw IllegalStateException("Please sign in")

    suspend fun signInAnonymously() {
        supabase.auth.signInAnonymously()
    }
}