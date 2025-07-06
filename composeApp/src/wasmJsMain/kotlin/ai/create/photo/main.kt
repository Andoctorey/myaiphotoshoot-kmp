package ai.create.photo

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.platform.getUrlHashManager
import ai.create.photo.ui.generate.Prompt
import ai.create.photo.ui.main.MainRoutes
import ai.create.photo.ui.main.navigateSingleTopTo
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
        var pendingPromptState by remember { mutableStateOf(ai.create.photo.ui.main.PromptState()) }

        App(
            navController = navController,
            initialPrompt = initialPrompt,
            pendingPromptState = pendingPromptState
        )

        LaunchedEffect(Unit) {
            val urlHashManager = getUrlHashManager()

            fun handleNavigation() {
                val rawHash = urlHashManager.getHash()
                val initRoute = rawHash.substringAfter('#', "")
                val params = parseParamsFromRoute(initRoute)
                val id = params["id"]

                // Extract the base route without query parameters
                val baseRoute = if (initRoute.contains("?")) {
                    initRoute.substringBefore("?")
                } else {
                    initRoute
                }

                if (baseRoute == MainRoutes.GENERATE) {
                    if (!id.isNullOrBlank()) {
                        launch {
                            try {
                                val generation = SupabaseFunction.getGeneration(id)
                                if (generation != null) {
                                    val prompt = Prompt(
                                        generationId = generation.id,
                                        text = generation.prompt,
                                        url = generation.imageUrl
                                    )
                                    pendingPromptState = pendingPromptState.update(prompt)
                                }
                                navController.navigateSingleTopTo(MainRoutes.GENERATE)
                            } catch (e: Exception) {
                                Logger.e("Failed to fetch generation", e)
                                navController.navigateSingleTopTo(MainRoutes.GENERATE)
                            }
                        }
                    } else {
                        navController.navigateSingleTopTo(MainRoutes.GENERATE)
                    }
                }
            }

            // Initial navigation
            handleNavigation()
            val isInitialNavigation = false

            // Hash change listener
            urlHashManager.addHashChangeListener { rawHash ->
                if (isInitialNavigation) return@addHashChangeListener
                val initRoute = rawHash.substringAfter('#', "")
                val urlParams = parseParamsFromRoute(rawHash.substringAfter('#', ""))
                val urlId = urlParams["id"]
                val baseRoute = if (initRoute.contains("?")) {
                    initRoute.substringBefore("?")
                } else {
                    initRoute
                }
                if (baseRoute == MainRoutes.GENERATE && !urlId.isNullOrBlank()) {
                    handleNavigation()
                } else if (baseRoute in listOf(
                        MainRoutes.GALLERY,
                        MainRoutes.GENERATE,
                        MainRoutes.SETTINGS
                    )
                ) {
                    when (baseRoute) {
                        MainRoutes.GALLERY -> navController.navigateSingleTopTo(MainRoutes.GALLERY)
                        MainRoutes.GENERATE -> navController.navigateSingleTopTo(MainRoutes.GENERATE)
                        MainRoutes.SETTINGS -> navController.navigateSingleTopTo(MainRoutes.SETTINGS)
                    }
                }
            }

            // Popstate event for browser navigation
            window.addEventListener("popstate") {
                val currentHash = urlHashManager.getHash()
                val urlParams = parseParamsFromRoute(currentHash.substringAfter('#', ""))
                val urlId = urlParams["id"]
                val baseRoute =
                    if (currentHash.contains("#${MainRoutes.GENERATE}")) MainRoutes.GENERATE else ""
                if (baseRoute == MainRoutes.GENERATE && !urlId.isNullOrBlank()) {
                    handleNavigation()
                }
            }

            // Focus event for tab refocus
            window.addEventListener("focus") {
                val currentHash = urlHashManager.getHash()
                val urlParams = parseParamsFromRoute(currentHash.substringAfter('#', ""))
                val urlId = urlParams["id"]
                val baseRoute =
                    if (currentHash.contains("#${MainRoutes.GENERATE}")) MainRoutes.GENERATE else ""
                if (baseRoute == MainRoutes.GENERATE && !urlId.isNullOrBlank()) {
                    handleNavigation()
                }
            }

            // Navigation binding
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