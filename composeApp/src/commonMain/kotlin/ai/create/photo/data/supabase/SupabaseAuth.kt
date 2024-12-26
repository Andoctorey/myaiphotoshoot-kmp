package ai.create.photo.data.supabase

import ai.create.photo.data.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.OtpType
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

    suspend fun verifyEmailOtp(email: String, code: String) {
        Logger.i("verifyEmailOtp $code")
        supabase.auth.verifyEmailOtp(
            type = OtpType.Email.EMAIL, email = email, token = code
        )
    }

    suspend fun convertAnonymousUserToEmail(email: String) {
        supabase.auth.updateUser {
            this.email = email
        }
    }
}