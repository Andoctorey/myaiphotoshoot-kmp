package ai.create.photo.data.supabase

import ai.create.photo.data.supabase.Supabase.supabase
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

object SupabaseAuth {

    private val signInMutex = Mutex()

    suspend fun signInAnonymously() {
        if (!signInMutex.tryLock()) return
        Logger.i("signInAnonymously")
        try {
            val maxAttempts = 3
            var attempt = 0
            var lastError: Throwable? = null
            while (attempt < maxAttempts) {
                try {
                    withContext(Dispatchers.Default) {
                        supabase.auth.signInAnonymously()
                    }
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
            signInMutex.unlock()
        }
    }

    suspend fun signInWithEmailOtp(email: String) {
        Logger.i("signInWithEmailOtp $email")
        withContext(Dispatchers.Default) {
            supabase.auth.signInWith(OTP) {
                this.email = email
            }
        }
    }

    suspend fun verifyEmailOtp(email: String, code: String) {
        Logger.i("verifyEmailOtp $code")
        withContext(Dispatchers.Default) {
            supabase.auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL, email = email, token = code
            )
        }
    }

    suspend fun convertAnonymousUserToEmail(email: String) {
        Logger.i("convertAnonymousUserToEmail $email")
        withContext(Dispatchers.Default) {
            supabase.auth.updateUser {
                this.email = email
            }
        }
    }
}
