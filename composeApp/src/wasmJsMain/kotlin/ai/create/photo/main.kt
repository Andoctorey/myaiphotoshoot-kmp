package ai.create.photo

import ai.create.photo.web.SystemTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLMetaElement
import org.w3c.dom.events.Event

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
        SystemTheme()
    }

    // Set up the media query for dark mode.
    val mediaQueryList = window.matchMedia("(prefers-color-scheme: dark)")

    // Function to update the theme color meta tag.
    fun updateThemeColor() {
        val metaTag = document.querySelector("meta[name='theme-color']") as? HTMLMetaElement
        metaTag?.content = if (mediaQueryList.matches) "#201A1B" else "#FFFBFF"
    }

    // Update theme immediately.
    updateThemeColor()

    // Register the listener for theme changes.
    val listener: (Event) -> Unit = { updateThemeColor() }
    mediaQueryList.addEventListener("change", listener)

    // Remove the loading element.
    document.getElementById("loading")?.remove()
}
