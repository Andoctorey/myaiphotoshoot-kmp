package ai.create.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "My AI Photo Shoot",
    ) {
        SetMinimumWindowSize(width = 100.dp, height = 200.dp)
        App()
    }
}

@Composable
fun FrameWindowScope.SetMinimumWindowSize(width: Dp, height: Dp) {
    val density = LocalDensity.current
    LaunchedEffect(density) {
        window.minimumSize = with(density) {
            Dimension(width.toPx().toInt(), height.toPx().toInt())
        }
    }
}