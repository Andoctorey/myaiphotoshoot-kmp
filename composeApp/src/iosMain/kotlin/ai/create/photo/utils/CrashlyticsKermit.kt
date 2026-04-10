package ai.create.photo.utils

import ai.create.photo.data.logger.FilteredCrashlyticsLogWriter
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger

private var isCrashlyticsInitialized = false

@OptIn(ExperimentalKermitApi::class)
fun setupCrashlytics() {
    if (isCrashlyticsInitialized) return

    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
    Logger.addLogWriter(FilteredCrashlyticsLogWriter())

    isCrashlyticsInitialized = true
}
