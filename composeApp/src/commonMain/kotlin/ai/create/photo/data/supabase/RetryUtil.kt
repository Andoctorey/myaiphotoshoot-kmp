package ai.create.photo.data.supabase

import co.touchlab.kermit.Logger
import coil3.network.HttpException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.io.EOFException
import kotlinx.io.IOException
import kotlin.math.pow
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

/**
 * Retry configuration for Supabase requests
 */
data class RetryConfig(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 1000,
    val maxDelayMs: Long = 10000,
    val exponentialBackoffMultiplier: Double = 2.0,
    val retryOnExceptions: List<KClass<out Throwable>> = listOf(
        HttpRequestException::class,
        IOException::class,
        UnresolvedAddressException::class,
        RestException::class,
        HttpException::class,
        EOFException::class,
        ConnectTimeoutException::class,
    )
)

/**
 * Extension function to retry any suspend function with exponential backoff
 */
suspend fun <T> retryWithBackoff(
    config: RetryConfig = RetryConfig(),
    operation: suspend () -> T
): T {
    var lastException: Throwable? = null

    for (attempt in 0..config.maxRetries) {
        try {
            return operation()
        } catch (e: Throwable) {
            lastException = e

            // Don't retry if it's not a network-related exception
            if (!config.retryOnExceptions.any { it.isInstance(e) }) {
                throw e
            }

            // Don't retry on the last attempt
            if (attempt == config.maxRetries) {
                break
            }

            val delayMs = minOf(
                config.initialDelayMs * config.exponentialBackoffMultiplier.pow(attempt).toLong(),
                config.maxDelayMs
            )

            Logger.w("Request failed (attempt ${attempt + 1}/${config.maxRetries + 1}), retrying in ${delayMs}ms. Error: ${e.message}")

            currentCoroutineContext().ensureActive()
            delay(delayMs.milliseconds)
        }
    }

    lastException?.let {
        Logger.e("All retry attempts failed after ${config.maxRetries + 1} attempts", it)
        throw it
    }

    error("Unreachable code")
}