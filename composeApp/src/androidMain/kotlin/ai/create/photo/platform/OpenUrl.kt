package ai.create.photo.platform

import ai.create.photo.app.App.Companion.context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import co.touchlab.kermit.Logger

actual fun openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Logger.e("failed to open $url", e)
        Toast.makeText(context, "Please open $url", Toast.LENGTH_LONG).show()
    }

}