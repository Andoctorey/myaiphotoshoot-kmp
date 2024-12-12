package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.status.SessionStatus.Authenticated
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

object SupabaseAuth {

    val sessionStatus = supabase.auth.sessionStatus.onEach {
        Logger.i("Auth status: $it")
        if (it is Authenticated) {
            Logger.i("userId: ${it.session.user?.id}")
        }
    }.stateIn(MainScope(), SharingStarted.Eagerly, SessionStatus.Initializing)

    val userId: String
        get() = (sessionStatus.value as? Authenticated)?.session?.user?.id
            ?: throw IllegalStateException("Please sign in")

    suspend fun signInAnonymously() {
        Logger.i("signInAnonymously")
        supabase.auth.signInAnonymously()
    }
}