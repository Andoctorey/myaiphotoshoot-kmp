package ai.create.photo.data.supabase

import co.touchlab.kermit.Logger
import coil3.network.HttpException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
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
        HttpRequestTimeoutException::class,
        IOException::class,
        UnresolvedAddressException::class,
        RestException::class,
        HttpException::class,
        EOFException::class,
        ConnectTimeoutException::class,
        SocketTimeoutException::class,
    )
)

private enum class NetworkIssueKind {
    TRANSIENT_TRANSPORT,
    CANCELLED_REQUEST,
    OTHER,
}

private val transientTransportTextMarkers = listOf(
    "connection reset by peer",
    "software caused connection abort",
    "prematurely closed the connection",
    "not enough data available",
    "unexpected end of stream",
    "stream was reset",
    "broken pipe",
    "socket closed",
    "connection refused",
    "network is unreachable",
    "host is unreachable",
    "unable to resolve host",
    "failed to connect",
    "failed to parse http response",
    "connect timeout",
    "socket timeout",
)

private val cancelledRequestTextMarkers = listOf(
    "request was cancelled",
    "request was canceled",
    "request cancelled",
    "request canceled",
    "request canceled due to",
)

private fun String.containsAny(markers: List<String>): Boolean = markers.any { it in this }

private fun Throwable.networkDiagnosticText(maxDepth: Int = 8): String {
    val parts = mutableListOf<String>()
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < maxDepth) {
        parts += current::class.simpleName.orEmpty()
        parts += current.message.orEmpty()
        current = current.cause
        depth++
    }
    return parts.joinToString(" ").lowercase()
}

private fun Throwable.classifyNetworkIssue(): NetworkIssueKind {
    if (this is HttpRequestException) {
        val message = this.message.orEmpty().lowercase()
        val hasSupabaseEndpoint =
            "supabase.co/" in message ||
                "/functions/v1/" in message ||
                "/storage/v1/" in message ||
                "/rest/v1/" in message
        val hasEmptyFailureMessage =
            "failed with message:" in message &&
                message.substringAfter("failed with message:", "").trim().isEmpty()
        if (hasSupabaseEndpoint && hasEmptyFailureMessage) {
            return NetworkIssueKind.TRANSIENT_TRANSPORT
        }
    }

    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < 8) {
        when (current) {
            is ConnectTimeoutException,
            is HttpRequestTimeoutException,
            is UnresolvedAddressException,
            is EOFException,
            is SocketTimeoutException -> return NetworkIssueKind.TRANSIENT_TRANSPORT
            is CancellationException -> {
                val text = current.message.orEmpty().lowercase()
                if (text.containsAny(cancelledRequestTextMarkers)) {
                    return NetworkIssueKind.CANCELLED_REQUEST
                }
            }
        }
        current = current.cause
        depth++
    }

    val text = networkDiagnosticText()
    if (text.containsAny(cancelledRequestTextMarkers)) return NetworkIssueKind.CANCELLED_REQUEST
    if (text.containsAny(transientTransportTextMarkers)) return NetworkIssueKind.TRANSIENT_TRANSPORT

    return NetworkIssueKind.OTHER
}

fun Throwable.isExpectedTransientNetworkIssue(): Boolean {
    return classifyNetworkIssue() == NetworkIssueKind.TRANSIENT_TRANSPORT
}

fun Throwable.isExpectedNetworkNoise(): Boolean {
    return classifyNetworkIssue() != NetworkIssueKind.OTHER
}

fun isExpectedNetworkNoise(
    @Suppress("UNUSED_PARAMETER") message: String?,
    throwable: Throwable?
): Boolean {
    return throwable?.isExpectedNetworkNoise() == true
}

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

            // Authentication/authorization failures are not transient and should not be retried.
            if (e is UnauthorizedRestException) {
                throw e
            }

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
        if (it.isExpectedTransientNetworkIssue()) {
            Logger.w("All retry attempts failed after ${config.maxRetries + 1} attempts: ${it.message}")
        } else {
            Logger.e("All retry attempts failed after ${config.maxRetries + 1} attempts", it)
        }
        throw it
    }

    error("Unreachable code")
}
