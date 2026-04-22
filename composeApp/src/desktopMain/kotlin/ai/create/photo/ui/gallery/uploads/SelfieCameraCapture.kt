package ai.create.photo.ui.gallery.uploads

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.core.PlatformFiles

actual fun supportsSelfieCameraCapture(): Boolean = false

@Composable
actual fun SelfieCameraCapture(
    uploadedCount: Int,
    targetCount: Int,
    onDismiss: () -> Unit,
    onPhotosCaptured: (PlatformFiles) -> Unit,
    onError: (Throwable) -> Unit,
) = Unit
