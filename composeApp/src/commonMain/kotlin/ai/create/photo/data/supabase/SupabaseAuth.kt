package ai.create.photo.data.supabase

import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.auth

object SupabaseAuth {

    suspend fun signInAnonymously() {
        Logger.i("signInAnonymously")
        Supabase.supabase.auth.signInAnonymously()
    }
}