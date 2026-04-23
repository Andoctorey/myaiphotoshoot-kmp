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
import org.jetbrains.compose.resources.getString
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.unknown_error

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
                if (lastError.isExpectedTransientNetworkIssue()) {
                    Logger.w(
                        "Anonymous sign-in failed after $maxAttempts attempts due to network issue: ${lastError.message}"
                    )
                } else {
                    Logger.e("Anonymous sign-in failed after $maxAttempts attempts", lastError)
                }
            }
        } finally {
            signInMutex.unlock()
        }
    }

    suspend fun signInWithEmailOtp(email: String) {
        Logger.i("signInWithEmailOtp $email")
        withNormalizedAuthErrors("signInWithEmailOtp") {
            withContext(Dispatchers.Default) {
                supabase.auth.signInWith(OTP) {
                    this.email = email
                }
            }
        }
    }

    suspend fun verifyEmailOtp(email: String, code: String) {
        Logger.i("verifyEmailOtp $code")
        withNormalizedAuthErrors("verifyEmailOtp") {
            withContext(Dispatchers.Default) {
                supabase.auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL, email = email, token = code
                )
            }
        }
    }

    suspend fun convertAnonymousUserToEmail(email: String) {
        Logger.i("convertAnonymousUserToEmail $email")
        withNormalizedAuthErrors("convertAnonymousUserToEmail") {
            withContext(Dispatchers.Default) {
                supabase.auth.updateUser {
                    this.email = email
                }
            }
        }
    }
}

class ExpectedAuthFailureException(message: String) : IllegalStateException(message)

private suspend fun <T> withNormalizedAuthErrors(
    operation: String,
    block: suspend () -> T
): T = try {
    block()
} catch (error: Throwable) {
    if (error is CancellationException) throw error
    if (error.isMalformedAuthErrorResponse()) {
        Logger.w("Supabase auth returned malformed error response during $operation", error)
        throw ExpectedAuthFailureException(getString(Res.string.unknown_error))
    }
    throw error
}

private fun Throwable.isMalformedAuthErrorResponse(): Boolean {
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < 8) {
        val className = current::class.simpleName.orEmpty().lowercase()
        val message = current.message.orEmpty().lowercase()
        if ("illegalargumentexception" in className && "is not a jsonobject" in message) {
            return true
        }
        current = current.cause
        depth++
    }
    return false
}
