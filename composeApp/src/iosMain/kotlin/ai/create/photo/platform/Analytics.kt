package ai.create.photo.platform


actual fun logUserEmail(email: String) {
    crashlyticsProvider?.logUserEmail(email)
}


actual fun logUserId(id: String) {
    crashlyticsProvider?.logUserId(id)
}

interface CrashlyticsProvider {
    fun logUserEmail(email: String)
    fun logUserId(id: String)
}

var crashlyticsProvider: CrashlyticsProvider? = null