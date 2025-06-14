package ai.create.photo.platform


actual fun logUserEmail(email: String) = crashlyticsProvider?.logUserEmail(email)
    ?: throw IllegalStateException("CrashlyticsProvider not set")


actual fun logUserId(id: String) =
    crashlyticsProvider?.logUserId(id)
        ?: throw IllegalStateException("CrashlyticsProvider not set")

interface CrashlyticsProvider {
    fun logUserEmail(email: String)
    fun logUserId(id: String)
}

var crashlyticsProvider: CrashlyticsProvider? = null