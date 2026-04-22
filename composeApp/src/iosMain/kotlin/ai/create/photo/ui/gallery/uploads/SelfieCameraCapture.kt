@file:OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package ai.create.photo.ui.gallery.uploads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.useContents
import kotlinx.cinterop.value
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.close_camera
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.CoreGraphics.CGRectZero
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreVideo.CVPixelBufferGetBaseAddressOfPlane
import platform.CoreVideo.CVPixelBufferGetBytesPerRowOfPlane
import platform.CoreVideo.CVPixelBufferGetHeightOfPlane
import platform.CoreVideo.CVPixelBufferGetWidthOfPlane
import platform.CoreVideo.CVPixelBufferIsPlanar
import platform.CoreVideo.CVPixelBufferLockBaseAddress
import platform.CoreVideo.CVPixelBufferRef
import platform.CoreVideo.CVPixelBufferUnlockBaseAddress
import platform.CoreVideo.kCVPixelBufferLock_ReadOnly
import platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey
import platform.CoreVideo.kCVPixelFormatType_420YpCbCr8BiPlanarFullRange
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToURL
import platform.QuartzCore.CACurrentMediaTime
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UIColor
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIView
import platform.Vision.VNDetectFaceRectanglesRequest
import platform.Vision.VNFaceObservation
import platform.Vision.VNImageRequestHandler
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import kotlin.math.PI

actual fun supportsSelfieCameraCapture(): Boolean = frontCameraDevice() != null

@Composable
actual fun SelfieCameraCapture(
    uploadedCount: Int,
    targetCount: Int,
    onDismiss: () -> Unit,
    onPhotosCaptured: (PlatformFiles) -> Unit,
    onError: (Throwable) -> Unit,
) {
    var permissionState by remember { mutableStateOf(cameraPermissionState()) }
    var appIsActive by remember { mutableStateOf(true) }
    var pendingCapturedCount by remember { mutableIntStateOf(0) }
    var acknowledgedUploadedCount by remember { mutableIntStateOf(uploadedCount) }
    var captureInFlight by remember { mutableStateOf(false) }
    var assessment by remember { mutableStateOf<SelfieFrameAssessment?>(null) }
    var completedVarietyGoals by remember { mutableStateOf(emptySet<SelfieVarietyGoal>()) }
    val guidance = remember(assessment) { assessSelfieFrame(assessment) }
    val completedVarietyMask = remember(completedVarietyGoals) {
        completedVarietyGoals.fold(0) { mask, goal -> mask or goal.mask }
    }
    val displayCount = (uploadedCount + pendingCapturedCount).coerceAtMost(targetCount)
    val hasReachedTarget = uploadedCount >= targetCount
    val topMessageRes = guidance.message.labelRes()
    val closeLabel = stringResource(Res.string.close_camera)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnError by rememberUpdatedState(onError)
    val currentOnPhotosCaptured by rememberUpdatedState(onPhotosCaptured)

    val cameraController = remember {
        IosSelfieCameraController(
            onAssessment = { newAssessment ->
                assessment = newAssessment
            },
            onCaptureSaved = { file ->
                captureInFlight = false
                pendingCapturedCount++
                completedVarietyGoals = completedVarietyGoals + classifySelfieVariety(assessment)
                Logger.i("ios selfie capture saved pending=$pendingCapturedCount uploaded=$uploadedCount goals=${completedVarietyGoals.size}")
                currentOnPhotosCaptured(listOf(file))
            },
            onCaptureFailed = { throwable ->
                captureInFlight = false
                Logger.e("Failed to capture iOS selfie photo", throwable)
                currentOnError(throwable)
            },
            onSessionFailed = { throwable ->
                Logger.e("Failed to start iOS selfie camera", throwable)
                currentOnError(throwable)
                currentOnDismiss()
            },
        )
    }

    DisposableEffect(Unit) {
        val notificationCenter = NSNotificationCenter.defaultCenter
        val didBecomeActiveObserver = notificationCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null,
        ) {
            Logger.i("ios selfie app became active")
            appIsActive = true
        }
        val willResignActiveObserver = notificationCenter.addObserverForName(
            name = UIApplicationWillResignActiveNotification,
            `object` = null,
            queue = null,
        ) {
            Logger.i("ios selfie app will resign active")
            appIsActive = false
        }
        val didEnterBackgroundObserver = notificationCenter.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null,
        ) {
            Logger.i("ios selfie app entered background")
            appIsActive = false
        }
        onDispose {
            notificationCenter.removeObserver(didBecomeActiveObserver)
            notificationCenter.removeObserver(willResignActiveObserver)
            notificationCenter.removeObserver(didEnterBackgroundObserver)
            cameraController.stop()
        }
    }

    LaunchedEffect(Unit) {
        Logger.i("ios selfie camera opened uploaded=$uploadedCount target=$targetCount")
        when (permissionState) {
            CameraPermissionState.NOT_DETERMINED -> {
                requestCameraAccess { granted ->
                    permissionState =
                        if (granted) CameraPermissionState.AUTHORIZED else CameraPermissionState.DENIED
                    if (!granted) {
                        Logger.w("ios selfie camera permission denied")
                        openAppSettings()
                        currentOnDismiss()
                    }
                }
            }

            CameraPermissionState.DENIED -> {
                Logger.w("ios selfie camera permission denied")
                openAppSettings()
                currentOnDismiss()
            }

            CameraPermissionState.AUTHORIZED -> Unit
        }
    }

    LaunchedEffect(permissionState, appIsActive) {
        if (permissionState == CameraPermissionState.AUTHORIZED && appIsActive) {
            cameraController.start()
        } else {
            cameraController.stop()
        }
    }

    LaunchedEffect(uploadedCount) {
        if (uploadedCount > acknowledgedUploadedCount) {
            val uploadedDelta = uploadedCount - acknowledgedUploadedCount
            pendingCapturedCount = (pendingCapturedCount - uploadedDelta).coerceAtLeast(0)
            acknowledgedUploadedCount = uploadedCount
            Logger.i("ios selfie uploads acknowledged delta=$uploadedDelta uploaded=$uploadedCount pending=$pendingCapturedCount")
        }
    }

    LaunchedEffect(hasReachedTarget) {
        if (hasReachedTarget) {
            Logger.i("ios selfie target reached uploaded=$uploadedCount target=$targetCount")
            currentOnDismiss()
        }
    }

    val canCapturePhoto =
        permissionState == CameraPermissionState.AUTHORIZED &&
                guidance.canCapture &&
                !captureInFlight &&
                !hasReachedTarget &&
                cameraController.isReadyToCapture()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            UIKitView(
                factory = {
                    cameraController.previewView
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    cameraController.attachPreview(view)
                },
                properties = UIKitInteropProperties(
                    isInteractive = true,
                    isNativeAccessibilityEnabled = false,
                ),
            )

            if (permissionState != CameraPermissionState.AUTHORIZED || !cameraController.isRunning) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SelfieGuidanceCardIos(messageRes = topMessageRes)
                    SelfieVarietyChipsIos(
                        completedGoalsMask = completedVarietyMask,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(16.dp),
            ) {
                SelfieProgressChipIos(
                    uploadedCount = displayCount,
                    targetCount = targetCount,
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .safeDrawingPadding()
                    .padding(16.dp),
            ) {
                IconButton(
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                        shape = CircleShape,
                    ),
                    onClick = currentOnDismiss,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = closeLabel,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 92.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ShutterButtonIos(
                    enabled = canCapturePhoto,
                    loading = captureInFlight,
                    onClick = {
                        if (!canCapturePhoto) return@ShutterButtonIos
                        Logger.i("ios selfie capture requested shown=$displayCount uploaded=$uploadedCount pending=$pendingCapturedCount")
                        captureInFlight = true
                        cameraController.capturePhoto()
                    },
                )
            }
        }
    }
}

@Composable
private fun SelfieProgressChipIos(
    uploadedCount: Int,
    targetCount: Int,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 4.dp,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            text = "${uploadedCount.coerceAtMost(targetCount)}/$targetCount",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ShutterButtonIos(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    val alpha = if (enabled) 1f else 0.5f
    Surface(
        modifier = Modifier.size(86.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        onClick = onClick,
        enabled = enabled && !loading,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            ) {
                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelfieVarietyChipsIos(
    completedGoalsMask: Int,
) {
    val supportedGoals = remember {
        listOf(
            SelfieVarietyGoal.FACE_FORWARD,
            SelfieVarietyGoal.LOOK_UP,
            SelfieVarietyGoal.LOOK_DOWN,
            SelfieVarietyGoal.LEFT_SIDE,
            SelfieVarietyGoal.RIGHT_SIDE,
            SelfieVarietyGoal.CLOSE_UP,
            SelfieVarietyGoal.UPPER_BODY,
        )
    }
    val nextVisibleGoal = remember(completedGoalsMask, supportedGoals) {
        supportedGoals.firstOrNull { completedGoalsMask and it.mask == 0 }
            ?: SelfieVarietyGoal.MORE_VARIETY
    }
    val chips = remember(completedGoalsMask, nextVisibleGoal) {
        buildList {
            supportedGoals.forEach { goal ->
                val isCompleted = completedGoalsMask and goal.mask != 0
                add(
                    SelfieVarietyChipStateIos(
                        goal,
                        isCompleted,
                        !isCompleted && goal == nextVisibleGoal
                    )
                )
            }
            if (supportedGoals.all { completedGoalsMask and it.mask != 0 }) {
                add(
                    SelfieVarietyChipStateIos(
                        SelfieVarietyGoal.MORE_VARIETY,
                        false,
                        nextVisibleGoal == SelfieVarietyGoal.MORE_VARIETY
                    )
                )
            }
        }
    }
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        chips.forEach { chip ->
            Surface(
                modifier = Modifier.padding(horizontal = 4.dp),
                shape = RoundedCornerShape(999.dp),
                color = when {
                    chip.isCompleted -> MaterialTheme.colorScheme.primary
                    chip.isNext -> MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                },
                contentColor = when {
                    chip.isCompleted -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                tonalElevation = if (chip.isCompleted || chip.isNext) 4.dp else 0.dp,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    text = stringResource(chip.goal.chipLabelRes()),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

private data class SelfieVarietyChipStateIos(
    val goal: SelfieVarietyGoal,
    val isCompleted: Boolean,
    val isNext: Boolean,
)

@Composable
private fun SelfieGuidanceCardIos(messageRes: StringResource) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
        tonalElevation = 6.dp,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private enum class CameraPermissionState {
    AUTHORIZED,
    NOT_DETERMINED,
    DENIED,
}

private fun cameraPermissionState(): CameraPermissionState = when (
    AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
) {
    AVAuthorizationStatusAuthorized -> CameraPermissionState.AUTHORIZED
    AVAuthorizationStatusNotDetermined -> CameraPermissionState.NOT_DETERMINED
    else -> CameraPermissionState.DENIED
}

private fun requestCameraAccess(onResult: (Boolean) -> Unit) {
    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
        dispatch_async(dispatch_get_main_queue()) {
            onResult(granted)
        }
    }
}

private fun openAppSettings() {
    val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    UIApplication.sharedApplication.openURL(
        url = settingsUrl,
        options = emptyMap<Any?, Any>(),
        completionHandler = null,
    )
}

private fun frontCameraDevice(): AVCaptureDevice? {
    val discoverySession = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
        deviceTypes = listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
        mediaType = AVMediaTypeVideo,
        position = AVCaptureDevicePositionFront,
    )
    return discoverySession.devices.firstOrNull() as? AVCaptureDevice
}

private class IosSelfieCameraController(
    private val onAssessment: (SelfieFrameAssessment) -> Unit,
    private val onCaptureSaved: (PlatformFile) -> Unit,
    private val onCaptureFailed: (Throwable) -> Unit,
    private val onSessionFailed: (Throwable) -> Unit,
) : NSObject(),
    AVCapturePhotoCaptureDelegateProtocol,
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

    val previewView = UIView(frame = CGRectZero.readValue()).apply {
        backgroundColor = UIColor.blackColor
    }

    private val session = AVCaptureSession().apply {
        sessionPreset = AVCaptureSessionPresetPhoto
    }
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }
    private val photoOutput = AVCapturePhotoOutput()
    private val videoOutput = AVCaptureVideoDataOutput()
    private val sessionQueue = dispatch_queue_create("ai.create.photo.selfie.session", null)
    private val analysisQueue = dispatch_queue_create("ai.create.photo.selfie.analysis", null)
    private val impactFeedbackGenerator = UIImpactFeedbackGenerator()

    var isRunning: Boolean by mutableStateOf(false)
        private set

    private var isConfigured = false
    private var isAnalyzing = false
    private var lastAnalysisTimestamp = 0.0

    fun isReadyToCapture(): Boolean = isConfigured && isRunning

    fun attachPreview(view: UIView) {
        if (previewLayer.superlayer != view.layer) {
            previewLayer.removeFromSuperlayer()
            view.layer.addSublayer(previewLayer)
        }
        previewLayer.frame = view.bounds
        previewLayer.connection?.videoOrientation = AVCaptureVideoOrientationPortrait
        previewLayer.connection?.videoMirrored = true
    }

    fun start() {
        if (isRunning) return
        dispatch_async(sessionQueue) {
            runCatching {
                if (!isConfigured) {
                    configureSession()
                    isConfigured = true
                }
                if (!session.running) {
                    Logger.i("ios selfie camera binding")
                    session.startRunning()
                }
                dispatch_async(dispatch_get_main_queue()) {
                    isRunning = true
                    impactFeedbackGenerator.prepare()
                    Logger.i("ios selfie camera bound")
                }
            }.onFailure { error ->
                dispatch_async(dispatch_get_main_queue()) {
                    onSessionFailed(error)
                }
            }
        }
    }

    fun stop() {
        dispatch_async(sessionQueue) {
            if (session.running) {
                session.stopRunning()
            }
            dispatch_async(dispatch_get_main_queue()) {
                isRunning = false
            }
        }
    }

    fun capturePhoto() {
        val settings = AVCapturePhotoSettings.photoSettings()
        dispatch_async(dispatch_get_main_queue()) {
            impactFeedbackGenerator.prepare()
        }
        dispatch_async(sessionQueue) {
            photoOutput.capturePhotoWithSettings(settings, delegate = this)
        }
    }

    private fun configureSession() {
        val camera = frontCameraDevice() ?: error("Front camera is not available on this device")
        memScoped {
            val errorPointer = alloc<ObjCObjectVar<NSError?>>()
            val input = AVCaptureDeviceInput.deviceInputWithDevice(camera, errorPointer.ptr)
                ?: error(
                    errorPointer.value?.localizedDescription
                        ?: "Unable to create iOS selfie camera input"
                )

            if (session.canAddInput(input)) {
                session.addInput(input)
            }
            if (session.canAddOutput(photoOutput)) {
                session.addOutput(photoOutput)
            }
            if (session.canAddOutput(videoOutput)) {
                videoOutput.alwaysDiscardsLateVideoFrames = true
                videoOutput.videoSettings = mapOf(
                    kCVPixelBufferPixelFormatTypeKey to kCVPixelFormatType_420YpCbCr8BiPlanarFullRange,
                )
                videoOutput.setSampleBufferDelegate(
                    this@IosSelfieCameraController,
                    queue = analysisQueue
                )
                session.addOutput(videoOutput)
            }
        }
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        val sampleBuffer = didOutputSampleBuffer ?: return
        val now = CACurrentMediaTime()
        if (isAnalyzing || now - lastAnalysisTimestamp < 0.16) return
        lastAnalysisTimestamp = now
        isAnalyzing = true
        val assessment = try {
            runCatching {
                analyzeSampleBuffer(sampleBuffer)
            }.getOrElse {
                Logger.w("Vision face analysis failed: ${it.message}")
                SelfieFrameAssessment(
                    faceCount = 0,
                    largestFaceRatio = 0f,
                    faceOffsetX = 0f,
                    faceOffsetY = 0f,
                    brightness = 0f,
                    headPitch = 0f,
                    headYaw = 0f,
                    headRoll = 0f,
                    smileProbability = null,
                )
            }
        } finally {
            isAnalyzing = false
        }
        dispatch_async(dispatch_get_main_queue()) {
            onAssessment(assessment)
        }
    }

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        if (error != null) {
            dispatch_async(dispatch_get_main_queue()) {
                onCaptureFailed(IllegalStateException(error.localizedDescription))
            }
            return
        }

        val data = didFinishProcessingPhoto.toFileDataRepresentation()
        if (data == null) {
            dispatch_async(dispatch_get_main_queue()) {
                onCaptureFailed(IllegalStateException("Unable to create selfie photo data"))
            }
            return
        }

        runCatching {
            val fileUrl = writeCapturedPhoto(data)
            playCaptureFeedback()
            dispatch_async(dispatch_get_main_queue()) {
                onCaptureSaved(PlatformFile(nsUrl = fileUrl))
            }
        }.onFailure { throwable ->
            dispatch_async(dispatch_get_main_queue()) {
                onCaptureFailed(throwable)
            }
        }
    }

    private fun analyzeSampleBuffer(sampleBuffer: CMSampleBufferRef): SelfieFrameAssessment {
        val imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)
            ?: return emptyAssessment()
        val request = VNDetectFaceRectanglesRequest()
        val handler =
            VNImageRequestHandler(cMSampleBuffer = sampleBuffer, options = emptyMap<Any?, Any>())
        memScoped {
            val errorPointer = alloc<ObjCObjectVar<NSError?>>()
            val success = handler.performRequests(listOf(request), error = errorPointer.ptr)
            if (!success) {
                throw IllegalStateException(
                    errorPointer.value?.localizedDescription ?: "Vision request failed"
                )
            }
        }

        val faces = (request.results ?: emptyList<Any>()).filterIsInstance<VNFaceObservation>()
        return toAssessment(
            faces = faces,
            brightness = estimateAverageLuma(imageBuffer),
        )
    }

    private fun writeCapturedPhoto(data: NSData): NSURL {
        val tempDirectory = NSTemporaryDirectory()
        val fileName = "selfie_${NSUUID().UUIDString}.jpg"
        val filePath = tempDirectory + fileName
        val fileUrl = NSURL.fileURLWithPath(filePath)
        if (!data.writeToURL(fileUrl, atomically = true)) {
            error("Unable to write selfie photo to $filePath")
        }
        return fileUrl
    }

    private fun playCaptureFeedback() {
        dispatch_async(dispatch_get_main_queue()) {
            impactFeedbackGenerator.impactOccurred()
            impactFeedbackGenerator.prepare()
        }
        AudioServicesPlaySystemSound(1108u)
    }
}

private fun emptyAssessment(): SelfieFrameAssessment = SelfieFrameAssessment(
    faceCount = 0,
    largestFaceRatio = 0f,
    faceOffsetX = 0f,
    faceOffsetY = 0f,
    brightness = 0f,
    headPitch = 0f,
    headYaw = 0f,
    headRoll = 0f,
    smileProbability = null,
)

private fun toAssessment(
    faces: List<VNFaceObservation>,
    brightness: Float,
): SelfieFrameAssessment {
    if (faces.isEmpty()) return emptyAssessment().copy(brightness = brightness)
    val primaryFace = faces.maxByOrNull { it.boundingBox.useContents { size.width * size.height } }
        ?: return emptyAssessment()
    val boundingBox = primaryFace.boundingBox
    val faceAreaRatio = boundingBox.useContents { (size.width * size.height).toFloat() }
    val faceCenterX = boundingBox.useContents { (origin.x + size.width / 2.0).toFloat() }
    val faceCenterYFromTop =
        boundingBox.useContents { (1.0 - (origin.y + size.height / 2.0)).toFloat() }
    return SelfieFrameAssessment(
        faceCount = faces.size,
        largestFaceRatio = faceAreaRatio,
        faceOffsetX = ((faceCenterX - 0.5f) / 0.5f),
        faceOffsetY = ((faceCenterYFromTop - 0.46f) / 0.5f),
        brightness = brightness,
        headPitch = radiansToDegrees(primaryFace.pitch?.doubleValue ?: 0.0),
        headYaw = radiansToDegrees(primaryFace.yaw?.doubleValue ?: 0.0),
        headRoll = radiansToDegrees(primaryFace.roll?.doubleValue ?: 0.0),
        smileProbability = null,
    )
}

private fun estimateAverageLuma(pixelBuffer: CVPixelBufferRef): Float {
    if (!CVPixelBufferIsPlanar(pixelBuffer)) return 100f
    CVPixelBufferLockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)
    return try {
        val baseAddress =
            CVPixelBufferGetBaseAddressOfPlane(pixelBuffer, 0u)?.reinterpret<ByteVar>()
                ?: return 100f
        val width = CVPixelBufferGetWidthOfPlane(pixelBuffer, 0u).toInt()
        val height = CVPixelBufferGetHeightOfPlane(pixelBuffer, 0u).toInt()
        val bytesPerRow = CVPixelBufferGetBytesPerRowOfPlane(pixelBuffer, 0u).toInt()
        val rowStep = 8
        val columnStep = 8
        var sum = 0L
        var count = 0
        var row = 0
        while (row < height) {
            val rowStart = row * bytesPerRow
            var column = 0
            while (column < width) {
                val samplePtr =
                    interpretCPointer<ByteVar>(baseAddress.rawValue + rowStart.toLong() + column.toLong())
                        ?: continue
                sum += samplePtr.pointed.value.toInt() and 0xFF
                count++
                column += columnStep
            }
            row += rowStep
        }
        if (count == 0) 100f else sum.toFloat() / count.toFloat()
    } finally {
        CVPixelBufferUnlockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly)
    }
}

private fun radiansToDegrees(radians: Double): Float = (radians * 180.0 / PI).toFloat()

private fun AVCapturePhoto.toFileDataRepresentation(): NSData? =
    performSelector(NSSelectorFromString("fileDataRepresentation")) as? NSData

private val SelfieVarietyGoal.mask: Int
    get() = 1 shl ordinal
