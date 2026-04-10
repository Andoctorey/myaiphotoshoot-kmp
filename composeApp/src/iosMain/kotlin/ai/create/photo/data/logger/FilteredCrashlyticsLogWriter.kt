package ai.create.photo.data.logger

import ai.create.photo.data.supabase.isExpectedNetworkNoise
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter

@OptIn(ExperimentalKermitApi::class)
class FilteredCrashlyticsLogWriter : LogWriter() {

    private val delegate = CrashlyticsLogWriter()

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity >= Severity.Error) {
            if (isExpectedNetworkNoise(message, throwable)) return
        }
        delegate.log(severity, message, tag, throwable)
    }
}
