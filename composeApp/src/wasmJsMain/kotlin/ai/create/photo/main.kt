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
        var currentGenerationId by remember { mutableStateOf<String?>(null) }
        var cachedPrompt by remember { mutableStateOf<Prompt?>(null) }

        App(
            navController = navController,
            initialPrompt = initialPrompt,
            pendingPromptState = pendingPromptState,
            currentGenerationId = currentGenerationId,
            onPromptCleared = { cachedPrompt = null }
        )

        LaunchedEffect(Unit) {
            val urlHashManager = getUrlHashManager()

            fun handleNavigation() {
                val rawHash = urlHashManager.getHash()
                val initRoute = rawHash.substringAfter('#', "")

                // Check if this is a generate route with generation ID
                if (initRoute.startsWith("${MainRoutes.GENERATE}/")) {
                    val generationId = initRoute.substringAfter("${MainRoutes.GENERATE}/")
                    if (generationId.isNotEmpty()) {
                        // Check if we have cached prompt data for this ID
                        if (cachedPrompt != null && cachedPrompt!!.generationId == generationId) {
                            pendingPromptState = pendingPromptState.update(cachedPrompt)
                            navController.navigate("${MainRoutes.GENERATE}/$generationId")
                        } else {
                            currentGenerationId = generationId  // Store the generation ID
                            launch {
                                try {
                                    val generation = SupabaseFunction.getGeneration(generationId)
                                    if (generation != null) {
                                        val prompt = Prompt(
                                            generationId = generation.id,
                                            text = generation.prompt,
                                            url = generation.imageUrl
                                        )
                                        pendingPromptState = pendingPromptState.update(prompt)
                                        cachedPrompt = prompt
                                    }
                                    navController.navigate("${MainRoutes.GENERATE}/$generationId")
                                } catch (e: Exception) {
                                    navController.navigate("${MainRoutes.GENERATE}/$generationId")
                                }
                            }
                        }
                    } else {
                        navController.navigateSingleTopTo(MainRoutes.GENERATE)
                    }
                } else if (initRoute == MainRoutes.GENERATE) {
                    navController.navigateSingleTopTo(MainRoutes.GENERATE)
                }
            }

            // Initial navigation
            handleNavigation()

            // Hash change listener
            urlHashManager.addHashChangeListener { rawHash ->
                val initRoute = rawHash.substringAfter('#', "")

                // Check if this is a generate route with generation ID
                if (initRoute.startsWith("${MainRoutes.GENERATE}/")) {
                    handleNavigation()
                } else if (initRoute in listOf(
                        MainRoutes.GALLERY,
                        MainRoutes.GENERATE,
                        MainRoutes.SETTINGS
                    )
                ) {
                    when (initRoute) {
                        MainRoutes.GALLERY -> navController.navigateSingleTopTo(MainRoutes.GALLERY)
                        MainRoutes.GENERATE -> navController.navigateSingleTopTo(MainRoutes.GENERATE)
                        MainRoutes.SETTINGS -> navController.navigateSingleTopTo(MainRoutes.SETTINGS)
                    }
                }
            }

            // Popstate event for browser navigation
            window.addEventListener("popstate") {
                val currentHash = urlHashManager.getHash()
                val initRoute = currentHash.substringAfter('#', "")
                if (initRoute.startsWith("${MainRoutes.GENERATE}/")) {
                    handleNavigation()
                }
            }

            // Focus event for tab refocus
            window.addEventListener("focus") {
                val currentHash = urlHashManager.getHash()
                val initRoute = currentHash.substringAfter('#', "")
                if (initRoute.startsWith("${MainRoutes.GENERATE}/")) {
                    handleNavigation()
                }
            }

            // Navigation binding
            window.bindToNavigation(navController)
        }
    }

    document.getElementById("loading")?.remove()
}

