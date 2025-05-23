package ai.create.photo

import ai.create.photo.ui.theme.tabsDark
import ai.create.photo.ui.theme.tabsLight
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.vinceglb.filekit.core.FileKit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        setContent {
            enableEdgeToEdge()
            UpdateNavigationBarColor()
            App()
        }
    }
}

@Composable
fun UpdateNavigationBarColor() {
    val view = LocalView.current
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val color = if (isDarkTheme) tabsDark else {
        tabsLight
    }

    SideEffect {
        val window = (context as Activity).window
        // deprecated but cannot navigationBarsPadding() because of wrong colors on
        // old and new androids with 3-button navigation
        window.navigationBarColor = color.toArgb()
        WindowCompat.getInsetsController(window, view)
            .isAppearanceLightNavigationBars = !isDarkTheme
    }
}