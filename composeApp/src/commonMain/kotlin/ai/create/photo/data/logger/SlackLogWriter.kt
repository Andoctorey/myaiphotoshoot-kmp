package ai.create.photo.data.logger

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.platform.Platforms
import ai.create.photo.platform.platform
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import coil3.network.HttpException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Direct Slack webhook URL (for non-browser platforms)
private const val SLACK_WEBHOOK_URL =
    "https://hooks.slack.com/services/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"

@Serializable
data class SlackPayload @OptIn(ExperimentalTime::class) constructor(
    @SerialName("text") val text: String,
    @SerialName("timestamp") val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    @SerialName("severity") val severity: String,
)

// Counter to limit error messages per session
private var errorCount = 0

class SlackLogWriter : LogWriter(), Closeable {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Using a Channel to rate-limit/batch log messages.
    private val errorChannel = Channel<SlackPayload>(
        capacity = 5,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        logScope.launch {
            for (payload in errorChannel) {
                try {
                    if (platform().platform == Platforms.WEB_DESKTOP ||
                        platform().platform == Platforms.WEB_MOBILE
                    ) {
                        SupabaseFunction.sendSlackError(payload.text)
                    } else {
                        httpClient.post(SLACK_WEBHOOK_URL) {
                            contentType(ContentType.Application.Json)
                            setBody(payload)
                        }
                    }
                    delay(5000L)
                } catch (e: Exception) {
                    Logger.w("Failed to send error log to Slack: ${e.message}")
                }
            }
        }
    }

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        if (severity == Severity.Error) {
            // Limit to max 5 error messages per session
            if (errorCount++ >= 5) return

            if (throwable?.message?.contains("JWT expired") != true) {
                if (throwable is HttpRequestException ||
                    throwable is IOException ||
                    throwable is UnresolvedAddressException ||
                    throwable is HttpException ||
                    throwable is RestException ||
                    throwable?.message?.contains("Fail to fetch") == true
                ) return
            }

            if (tag == "Supabase-Core") return

            val errorText = buildString {
                append("[$tag] $message\n")
                throwable?.let { append("\nException: ${it.stackTraceToString()}") }
            }

            val payload = SlackPayload(
                text = errorText,
                severity = severity.name
            )

            logScope.launch { errorChannel.send(payload) }
        }
    }

    override fun close() {
        logScope.cancel()
        httpClient.close()
    }
}
