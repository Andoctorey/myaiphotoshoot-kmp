package ai.create.photo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    val body = document.body ?: return

    ComposeViewport(body) {
        val navController = rememberNavController()
        App(navController)

        LaunchedEffect(Unit) {
            val initRoute = window.location.hash.substringAfter('#', "")
            val params = parseParamsFromRoute(initRoute)
            val id = params["id"]
            if (!id.isNullOrBlank()) {
                Logger.i("id: $id")
                // doesn't work for now
//                navController.navigate(SettingsTab)
            }

            window.bindToNavigation(navController)
        }
    }

    document.getElementById("loading")?.remove()
}

fun parseParamsFromRoute(route: String): Map<String, String> {
    val queryStart = route.indexOf('?')
    if (queryStart == -1) return emptyMap()
    val query = route.substring(queryStart + 1)
    return query.split('&')
        .filter { it.isNotEmpty() }.mapNotNull { param ->
            val parts = param.split('=', limit = 2)
            if (parts.size == 2 && parts[0].isNotEmpty()) {
                parts[0] to parts[1]
            } else if (parts.size == 1 && parts[0].isNotEmpty()) {
                parts[0] to ""
            } else {
                null
            }
        }
        .toMap()
}