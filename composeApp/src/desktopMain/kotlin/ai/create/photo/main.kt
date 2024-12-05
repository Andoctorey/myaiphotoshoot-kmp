package ai.create.photo

import ai.create.photo.platform.firebaseOptions
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import java.awt.Dimension

fun main() = application {
    initFirebase()
    Window(
        onCloseRequest = ::exitApplication,
        title = "PhotoCreateAi",
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

private fun initFirebase() {
    FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
        val storage = mutableMapOf<String, String>()
        override fun store(key: String, value: String) = storage.set(key, value)
        override fun retrieve(key: String) = storage[key]
        override fun clear(key: String) {
            storage.remove(key)
        }

        override fun log(msg: String) = Logger.d(msg)
    })
    Firebase.initialize(
        context = Context(),
        options = firebaseOptions
    )
}