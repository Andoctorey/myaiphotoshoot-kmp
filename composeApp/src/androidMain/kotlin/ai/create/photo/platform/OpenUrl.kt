package ai.create.photo.platform

import ai.create.photo.app.App.Companion.context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.net.toUri
import co.touchlab.kermit.Logger

actual fun openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (shouldForceBrowserForUrl(url)) {
            val packageManager = context.packageManager
            val browserPackage = findBrowserPackage(packageManager)
            if (browserPackage != null) {
                intent.setPackage(browserPackage)
            }
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Logger.e("failed to open $url", e)
        Toast.makeText(context, "Please open $url", Toast.LENGTH_LONG).show()
    }

}

private fun shouldForceBrowserForUrl(url: String): Boolean {
    val uri = runCatching { url.toUri() }.getOrNull() ?: return false
    val host = uri.host?.lowercase() ?: return false
    if (host !in setOf("myaiphotoshoot.com", "www.myaiphotoshoot.com")) return false
    val path = uri.path?.lowercase() ?: return false
    return path == "/support" || path.startsWith("/support/") ||
            path.endsWith("/support") || path.contains("/support/")
}

@Suppress("DEPRECATION")
private fun findBrowserPackage(
    packageManager: PackageManager,
): String? {
    val myPackageName = context.packageName

    val defaultBrowserPackage = Intent
        .makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
        .resolveActivity(packageManager)
        ?.packageName
        ?.takeIf { it != myPackageName && isLaunchableBrowserPackage(packageManager, it) }
    if (defaultBrowserPackage != null) return defaultBrowserPackage

    val genericBrowserIntent = Intent(Intent.ACTION_VIEW, "https://www.google.com".toUri()).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
    return packageManager.queryIntentActivities(
        genericBrowserIntent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
        .mapNotNull { it.activityInfo?.packageName }
        .distinct()
        .firstOrNull { it != myPackageName }
}

private fun isLaunchableBrowserPackage(
    packageManager: PackageManager,
    packageName: String,
): Boolean {
    val webIntent = Intent(Intent.ACTION_VIEW, "https://www.google.com".toUri()).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        setPackage(packageName)
    }
    return webIntent.resolveActivity(packageManager) != null
}
