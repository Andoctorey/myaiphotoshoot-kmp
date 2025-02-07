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
import kotlinx.coroutines.*
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val SLACK_WEBHOOK_URL =
    "https://hooks.slack.com/services/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"

@Serializable
data class SlackPayload(val text: String)

class SlackLogWriter : LogWriter() {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json)
        }
    }

    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun log(
        severity: Severity,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {

        // ai.create.photo.ui.compose.PlaceHoldersKt.getFriendlyError
        if (throwable is HttpRequestException || throwable is IOException || throwable is UnresolvedAddressException) {
            return
        }
   
        if (severity == Severity.Error) {
            val errorText = buildString {
                append("[$tag] $message")
                throwable?.let { append("\nException: ${it.stackTraceToString()}") }
            }

            val payload = SlackPayload(text = errorText)

            logScope.launch {
                try {
                    httpClient.post(SLACK_WEBHOOK_URL) {
                        contentType(ContentType.Application.Json)
                        setBody(payload)
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
}
