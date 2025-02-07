package ai.create.photo.data.logger

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Clock.System
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val SLACK_WEBHOOK_URL =
    "https://hooks.slack.com/services/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"


@Serializable
data class SlackPayload(
    val text: String,
    val timestamp: Long = System.now().toEpochMilliseconds(),
    val severity: String
)

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

    // Optional: Using a Channel to rate-limit/batch log messages.
    private val errorChannel = Channel<SlackPayload>(Channel.UNLIMITED)

    init {
        logScope.launch {
            for (payload in errorChannel) {
                try {
                    httpClient.post(SLACK_WEBHOOK_URL) {
                        contentType(ContentType.Application.Json)
                        setBody(payload)
                    }
                    delay(1000L)
                } catch (e: Exception) {
                    println("Failed to send error log to Slack: ${e.message}")
                }
            }
        }
    }

    override fun log(
        severity: Severity,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        if (severity == Severity.Error) {
            if (throwable is HttpRequestException ||
                throwable is IOException ||
                throwable is UnresolvedAddressException
            ) {
                return
            }

            val errorText = buildString {
                append("[$tag] $message")
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
