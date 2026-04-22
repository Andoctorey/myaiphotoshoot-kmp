@file:OptIn(ExperimentalForeignApi::class)

package ai.create.photo.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.getString
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.photo_permission_settings_toast
import platform.Foundation.NSError
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("UNUSED_PARAMETER")
actual suspend fun saveGeneratedPhoto(
    bytes: ByteArray,
    baseName: String,
    extension: String,
) {
    val image = UIImage(data = bytes.toNSData())
    val photoPermissionMessage = getString(Res.string.photo_permission_settings_toast)

    val authorizationStatus = suspendCancellableCoroutine { continuation ->
        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
            continuation.resume(status)
        }
    }
    if (authorizationStatus != PHAuthorizationStatusAuthorized &&
        authorizationStatus != PHAuthorizationStatusLimited
    ) {
        throw IllegalStateException(photoPermissionMessage)
    }

    suspendCancellableCoroutine { continuation ->
        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                PHAssetChangeRequest.creationRequestForAssetFromImage(image)
            },
            completionHandler = { success, error ->
                when {
                    success -> continuation.resume(Unit)
                    error != null -> continuation.resumeWithException(error.asThrowable())
                    else -> continuation.resumeWithException(
                        IllegalStateException("Failed to save the photo to Photos.")
                    )
                }
            },
        )
    }
}

private fun NSError.asThrowable(): Throwable =
    IllegalStateException(localizedDescription)
