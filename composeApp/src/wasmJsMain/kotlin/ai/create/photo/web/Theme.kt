package ai.create.photo.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLMetaElement
import org.w3c.dom.events.Event

@Composable
fun SystemTheme() {
    DisposableEffect(Unit) {
        val mediaQueryList = window.matchMedia("(prefers-color-scheme: dark)")

        fun updateThemeColor() {
            val metaTag = document.querySelector("meta[name='theme-color']") as? HTMLMetaElement
            metaTag?.content = if (mediaQueryList.matches) "#201A1B" else "#FFFBFF"
        }

        updateThemeColor()

        val listener: (Event) -> Unit = { updateThemeColor() }
        mediaQueryList.addEventListener("change", listener)

        onDispose {
            mediaQueryList.removeEventListener("change", listener)
        }
    }
}
