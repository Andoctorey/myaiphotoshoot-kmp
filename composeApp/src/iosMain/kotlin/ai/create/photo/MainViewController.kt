package ai.create.photo

import ai.create.photo.utils.setupCrashlytics
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    LaunchedEffect(Unit) { setupCrashlytics() }
    App()
}