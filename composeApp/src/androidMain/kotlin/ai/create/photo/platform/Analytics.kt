package ai.create.photo.platform

import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin

actual fun logUserEmail(email: String) {
    CrashlyticsKotlin.setCustomValue("email", email)
}

actual fun logUserId(id: String) {
    CrashlyticsKotlin.setUserId(id)
}