package ai.create.photo.data.logger

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter

@OptIn(ExperimentalKermitApi::class)
class FilteredCrashlyticsLogWriter : LogWriter() {

    private val delegate = CrashlyticsLogWriter()

    private fun isExpectedNetworkNoise(message: String?, throwable: Throwable?): Boolean {
        val msg = (message ?: "") + " " + (throwable?.message ?: "")
        val lower = msg.lowercase()
        return when {
            // Common transient socket errors we don't want in Crashlytics noise
            "connection reset by peer" in lower -> true
            "software caused connection abort" in lower -> true
            "canceled" in lower && "request" in lower -> true // request canceled during navigation
            // Transient read/stream errors from Ktor/HTTP2/server closing
            "prematurely closed the connection" in lower -> true
            "not enough data available" in lower -> true
            "eofexception" in lower -> true
            ("eof" in lower && ("read" in lower || "byte" in lower)) -> true
            // British spelling variant often used by Ktor
            "was cancelled" in lower && "request" in lower -> true
            "cancelled" in lower && "request" in lower -> true
            else -> false
        }
    }

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity >= Severity.Error) {
            if (tag == "Supabase-Core" && isExpectedNetworkNoise(message, throwable)) return
            if (tag == "io.ktor.client" && isExpectedNetworkNoise(message, throwable)) return
        }
        delegate.log(severity, message, tag, throwable)
    }
}


