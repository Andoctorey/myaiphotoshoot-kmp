package ai.create.photo.data.supabase

import ai.create.photo.data.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

object SupabaseAuth {

    var signingIn: Boolean = false

    suspend fun signInAnonymously() {
        if (signingIn) return
        Logger.i("signInAnonymously")
        signingIn = true
        try {
            val maxAttempts = 3
            var attempt = 0
            var lastError: Throwable? = null
            while (attempt < maxAttempts) {
                try {
                    supabase.auth.signInAnonymously()
                    lastError = null
                    break
                } catch (e: Throwable) {
                    if (e is CancellationException) throw e
                    lastError = e
                    Logger.w("Anonymous sign-in attempt ${attempt + 1} failed", e)
                    val backoffMs = 1000L shl attempt
                    delay(backoffMs)
                    attempt++
                }
            }
            if (lastError != null) {
                Logger.e("Anonymous sign-in failed after $maxAttempts attempts", lastError)
            }
        } finally {
            signingIn = false
        }
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
        supabase.auth.updateUser {
            this.email = email
        }
    }
}