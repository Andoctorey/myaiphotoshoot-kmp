package ai.create.photo

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.ui.generate.Prompt
import ai.create.photo.ui.main.MainRoutes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    val body = document.body ?: return

    ComposeViewport(body) {
        val navController = rememberNavController()
        var initialPrompt by remember { mutableStateOf<Prompt?>(null) }

        App(navController = navController, initialPrompt = initialPrompt)

        LaunchedEffect(Unit) {
            val rawHash = window.location.hash
            val initRoute = rawHash.substringAfter('#', "")
            val params = parseParamsFromRoute(initRoute)
            val id = params["id"]
            Logger.i("Extracted id from URL: $id")

            if (!id.isNullOrBlank()) {
                Logger.i("Fetching generation for id: $id")
                launch {
                    try {
                        val generation = SupabaseFunction.getGeneration(id)
                        if (generation != null) {
                            Logger.i("Generation found, navigating to generate tab")
                            val prompt = Prompt(
                                generationId = generation.id,
                                text = generation.prompt,
                                url = generation.imageUrl,
                            )
                            initialPrompt = prompt
                            navController.navigate(MainRoutes.GENERATE)
                        } else {
                            Logger.w("Generation not found for id: $id")
                        }
                    } catch (e: Exception) {
                        Logger.e("Failed to fetch generation", e)
                    }
                }
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