package ai.create.photo.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    if (!enabled) return

    DisposableEffect(enabled) {
        window.history.pushState(null, "", window.location.href)

        val listener: (Event) -> Unit = {
            onBack()
            window.history.pushState(null, "", window.location.href)
        }
        window.addEventListener("popstate", listener)

        onDispose {
            window.removeEventListener("popstate", listener)
        }
    }
}
