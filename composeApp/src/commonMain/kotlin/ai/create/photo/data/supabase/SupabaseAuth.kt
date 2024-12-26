package ai.create.photo.data.supabase

import ai.create.photo.data.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP

object SupabaseAuth {

    suspend fun signInAnonymously() {
        Logger.i("signInAnonymously")
        supabase.auth.signInAnonymously()
    }

    suspend fun signInWithEmailOtp(email: String) {
        Logger.i("signInWithEmailOtp $email")
        supabase.auth.signInWith(OTP) {
            this.email = email
        }
    }
}