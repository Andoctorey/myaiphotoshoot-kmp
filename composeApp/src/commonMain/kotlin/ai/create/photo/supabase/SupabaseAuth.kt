package ai.create.photo.supabase

import ai.create.photo.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.auth

object SupabaseAuth {

    suspend fun signInAnonymously() {
        Logger.i("signInAnonymously")
        supabase.auth.signInAnonymously()
    }
}