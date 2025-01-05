package ai.create.photo.data.supabase

import ai.create.photo.data.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.OTP

object SupabaseAuth {

    var signingIn: Boolean = false

    suspend fun signInAnonymously() {
        if (signingIn) return
        Logger.i("signInAnonymously")
        signingIn = true
        supabase.auth.signInAnonymously()
        signingIn = false
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
        Logger.i("convertAnonymousUserToEmail $email")
        try {
            supabase.auth.updateUser {
                this.email = email
            }
        } catch (e: AuthRestException) {
            Logger.i("convertAnonymousUserToEmail", e)
        }
    }
}