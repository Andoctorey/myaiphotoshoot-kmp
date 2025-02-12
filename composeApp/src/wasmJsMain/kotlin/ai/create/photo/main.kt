package ai.create.photo

import ai.create.photo.web.SystemTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
        SystemTheme()
    }

    document.getElementById("loading")?.remove()
}
