package ai.create.photo.ui.gallery.uploads

import ai.create.photo.app.App
import ai.create.photo.core.extention.toast
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaActionSound
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import co.touchlab.kermit.Logger
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.camera_permission_settings_toast
import photocreateai.composeapp.generated.resources.close_camera
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual fun supportsSelfieCameraCapture(): Boolean =
    runCatching {
        val context = App.context
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return@runCatching false
        }
        val cameraManager =
            context.getSystemService(CameraManager::class.java) ?: return@runCatching false
        cameraManager.cameraIdList.any { cameraId ->
            cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        }
    }.getOrDefault(false)

@Composable
actual fun SelfieCameraCapture(
    uploadedCount: Int,
    targetCount: Int,
    onDismiss: () -> Unit,
    onPhotosCaptured: (PlatformFiles) -> Unit,
    onError: (Throwable) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var hasRequestedPermission by remember { mutableStateOf(hasPermission) }
    val permissionSettingsToast = stringResource(Res.string.camera_permission_settings_toast)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        hasRequestedPermission = true
        if (!granted) {
            Logger.w("selfie camera permission denied")
            context.toast(permissionSettingsToast)
            context.openAppSettings()
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission && !hasRequestedPermission) {
            hasRequestedPermission = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        return
    }

    val detector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.12f)
                .enableTracking()
                .build()
        )
    }
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val shutterFeedback = remember {
        MediaActionSound().apply {
            load(MediaActionSound.SHUTTER_CLICK)
        }
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var cameraSession by remember { mutableStateOf<BoundCameraSession?>(null) }
    var captureInFlight by remember { mutableStateOf(false) }
    var pendingCapturedCount by remember { mutableIntStateOf(0) }
    var acknowledgedUploadedCount by remember { mutableIntStateOf(uploadedCount) }
    var assessment by remember { mutableStateOf<SelfieFrameAssessment?>(null) }
    var completedVarietyGoals by remember { mutableStateOf(emptySet<SelfieVarietyGoal>()) }
    val guidance = remember(assessment) { assessSelfieFrame(assessment) }
    val nextVarietyGoal =
        remember(completedVarietyGoals) { nextSelfieVarietyGoal(completedVarietyGoals) }
    val completedVarietyMask = remember(completedVarietyGoals) {
        completedVarietyGoals.fold(0) { mask, goal -> mask or goal.mask }
    }
    val closeLabel = stringResource(Res.string.close_camera)
    val currentCameraSession by rememberUpdatedState(cameraSession)
    val imageCapture = cameraSession?.imageCapture
    LaunchedEffect(Unit) {
        Logger.i("selfie camera opened uploaded=$uploadedCount target=$targetCount")
    }
    LaunchedEffect(uploadedCount) {
        if (uploadedCount > acknowledgedUploadedCount) {
            val uploadedDelta = uploadedCount - acknowledgedUploadedCount
            pendingCapturedCount =
                (pendingCapturedCount - (uploadedCount - acknowledgedUploadedCount))
                    .coerceAtLeast(0)
            acknowledgedUploadedCount = uploadedCount
            Logger.i("selfie uploads acknowledged delta=$uploadedDelta uploaded=$uploadedCount pending=$pendingCapturedCount")
        }
    }
    val displayCount = (uploadedCount + pendingCapturedCount).coerceAtMost(targetCount)
    val hasReachedTarget = uploadedCount >= targetCount
    val canCapturePhoto =
        imageCapture != null && guidance.canCapture && !captureInFlight && !hasReachedTarget
    val topMessageRes = guidance.message.labelRes()
    LaunchedEffect(hasReachedTarget) {
        if (hasReachedTarget) {
            Logger.i("selfie target reached uploaded=$uploadedCount target=$targetCount")
            onDismiss()
        }
    }

    DisposableEffect(detector, cameraExecutor, shutterFeedback) {
        onDispose {
            currentCameraSession?.unbind()
            detector.close()
            cameraExecutor.shutdown()
            shutterFeedback.release()
        }
    }

    LaunchedEffect(lifecycleOwner) {
        runCatching {
            Logger.i("selfie camera binding")
            val provider = context.awaitCameraProvider()
            val displayRotation = previewView.display?.rotation ?: Surface.ROTATION_0
            val preview = Preview.Builder()
                .setTargetRotation(displayRotation)
                .build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
            val capture = ImageCapture.Builder()
                .setTargetRotation(displayRotation)
                .build()
            val analysis = buildImageAnalysis(
                detector = detector,
                executor = cameraExecutor,
                targetRotation = displayRotation,
            ) { assessment = it }
            cameraSession?.unbind()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                capture,
                analysis,
            )
            cameraSession = BoundCameraSession(
                provider = provider,
                preview = preview,
                imageCapture = capture,
                analysis = analysis,
            )
            Logger.i("selfie camera bound rotation=$displayRotation")
        }.onFailure {
            cameraSession = null
            Logger.e("Failed to bind selfie camera", it)
            onError(it)
            onDismiss()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { previewView },
            )
            if (cameraSession == null) {
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
                    SelfieGuidanceCard(messageRes = topMessageRes)
                    SelfieVarietyChips(
                        completedGoalsMask = completedVarietyMask,
                        nextGoal = nextVarietyGoal,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(16.dp),
            ) {
                SelfieProgressChip(
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
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            shape = CircleShape,
                        ),
                    onClick = onDismiss,
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
                ShutterButton(
                    enabled = canCapturePhoto,
                    loading = captureInFlight,
                    onClick = shutterClick@{
                        val capture = imageCapture ?: return@shutterClick
                        if (!canCapturePhoto) return@shutterClick
                        Logger.i("selfie capture requested shown=$displayCount uploaded=$uploadedCount pending=$pendingCapturedCount")
                        captureInFlight = true
                        captureSelfiePhoto(
                            shutterFeedback = shutterFeedback,
                            previewView = previewView,
                            imageCapture = capture,
                            onSuccess = { file ->
                                captureInFlight = false
                                pendingCapturedCount++
                                completedVarietyGoals =
                                    completedVarietyGoals + classifySelfieVariety(assessment)
                                Logger.i(
                                    "selfie capture saved pending=$pendingCapturedCount uploaded=$uploadedCount goals=${completedVarietyGoals.size}"
                                )
                                onPhotosCaptured(listOf(file))
                            },
                            onFailure = {
                                captureInFlight = false
                                Logger.e("Failed to capture selfie photo", it)
                                onError(it)
                            },
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SelfieProgressChip(
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
private fun ShutterButton(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    size: Dp = 86.dp,
) {
    val outerAlpha = if (enabled) 0.98f else 0.55f
    val innerAlpha = if (enabled) 1f else 0.5f
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .border(
                width = 4.dp,
                color = Color.White.copy(alpha = outerAlpha),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size - 18.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = innerAlpha),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelfieVarietyChips(
    completedGoalsMask: Int,
    nextGoal: SelfieVarietyGoal?,
) {
    val chips = remember(completedGoalsMask, nextGoal) {
        buildList {
            requiredSelfieVarietyGoals().forEach { goal ->
                val isCompleted = completedGoalsMask and goal.mask != 0
                add(
                    SelfieVarietyChipState(
                        goal = goal,
                        isCompleted = isCompleted,
                        isNext = !isCompleted && goal == nextGoal
                    )
                )
            }
            if (requiredSelfieVarietyGoals().all { completedGoalsMask and it.mask != 0 }) {
                add(
                    SelfieVarietyChipState(
                        goal = SelfieVarietyGoal.MORE_VARIETY,
                        isCompleted = false,
                        isNext = nextGoal == SelfieVarietyGoal.MORE_VARIETY,
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
                shadowElevation = if (chip.isCompleted) 2.dp else 0.dp,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    text = stringResource(chip.goal.chipLabelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (chip.isNext) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        Color.Unspecified
                    },
                )
            }
        }
    }
}

@Immutable
private data class SelfieVarietyChipState(
    val goal: SelfieVarietyGoal,
    val isCompleted: Boolean,
    val isNext: Boolean,
)

private fun buildImageAnalysis(
    detector: FaceDetector,
    executor: ExecutorService,
    targetRotation: Int,
    onAssessment: (SelfieFrameAssessment) -> Unit,
): ImageAnalysis {
    val inFlight = AtomicBoolean(false)
    return ImageAnalysis.Builder()
        .setTargetRotation(targetRotation)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { analysis ->
            analysis.setAnalyzer(
                executor,
            ) { imageProxy ->
                if (!inFlight.compareAndSet(false, true)) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                imageProxy.processWith(detector) { assessment ->
                    onAssessment(assessment)
                    inFlight.set(false)
                }
            }
        }
}

@ExperimentalGetImage
private fun ImageProxy.processWith(
    detector: FaceDetector,
    onFinished: (SelfieFrameAssessment) -> Unit,
) {
    val mediaImage = image ?: run {
        close()
        onFinished(
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
        )
        return
    }
    val inputImage = InputImage.fromMediaImage(mediaImage, imageInfo.rotationDegrees)
    val frameBrightness = planes.firstOrNull()?.buffer?.let(::estimateAverageLuma) ?: 0f
    detector.process(inputImage)
        .addOnSuccessListener { faces ->
            onFinished(
                toAssessment(
                    faces = faces,
                    width = width,
                    height = height,
                    rotationDegrees = imageInfo.rotationDegrees,
                    brightness = frameBrightness,
                )
            )
        }
        .addOnFailureListener {
            Logger.w("ML Kit face detection failed: ${it.message}")
            onFinished(
                SelfieFrameAssessment(
                    faceCount = 0,
                    largestFaceRatio = 0f,
                    faceOffsetX = 0f,
                    faceOffsetY = 0f,
                    brightness = frameBrightness,
                    headPitch = 0f,
                    headYaw = 0f,
                    headRoll = 0f,
                    smileProbability = null,
                )
            )
        }
        .addOnCompleteListener {
            close()
        }
}

private fun toAssessment(
    faces: List<Face>,
    width: Int,
    height: Int,
    rotationDegrees: Int,
    brightness: Float,
): SelfieFrameAssessment {
    val isQuarterTurn = rotationDegrees == 90 || rotationDegrees == 270
    val frameWidth = if (isQuarterTurn) height else width
    val frameHeight = if (isQuarterTurn) width else height
    if (faces.isEmpty()) {
        return SelfieFrameAssessment(0, 0f, 0f, 0f, brightness, 0f, 0f, 0f, null)
    }
    val primaryFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }!!
    val faceCenterX = primaryFace.boundingBox.centerX().toFloat()
    val faceCenterY = primaryFace.boundingBox.centerY().toFloat()
    val frameCenterX = frameWidth / 2f
    val targetFaceCenterY = frameHeight * 0.46f
    val faceAreaRatio =
        (primaryFace.boundingBox.width().toFloat() * primaryFace.boundingBox.height().toFloat()) /
                (frameWidth.toFloat() * frameHeight.toFloat())
    return SelfieFrameAssessment(
        faceCount = faces.size,
        largestFaceRatio = faceAreaRatio,
        faceOffsetX = (faceCenterX - frameCenterX) / frameCenterX,
        faceOffsetY = (faceCenterY - targetFaceCenterY) / (frameHeight / 2f),
        brightness = brightness,
        headPitch = primaryFace.headEulerAngleX,
        headYaw = primaryFace.headEulerAngleY,
        headRoll = primaryFace.headEulerAngleZ,
        smileProbability = primaryFace.smilingProbability?.takeIf { it >= 0f },
    )
}

private fun estimateAverageLuma(buffer: java.nio.ByteBuffer): Float {
    val duplicate = buffer.duplicate()
    duplicate.rewind()
    if (!duplicate.hasRemaining()) return 0f
    var sum = 0L
    val sampleStride = 4
    var count = 0
    while (duplicate.hasRemaining()) {
        sum += (duplicate.get().toInt() and 0xFF)
        count++
        repeat(sampleStride - 1) {
            if (duplicate.hasRemaining()) duplicate.get()
        }
    }
    return if (count == 0) 0f else sum.toFloat() / count.toFloat()
}

private suspend fun Context.awaitCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess(continuation::resume)
                    .onFailure(continuation::resumeWithException)
            },
            ContextCompat.getMainExecutor(this),
        )
    }

private fun Context.openAppSettings() {
    runCatching {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }.onFailure {
        Logger.w("Failed to open app settings", it)
    }
}

private data class BoundCameraSession(
    val provider: ProcessCameraProvider,
    val preview: Preview,
    val imageCapture: ImageCapture,
    val analysis: ImageAnalysis,
)

private fun BoundCameraSession.unbind() {
    runCatching {
        provider.unbind(preview, imageCapture, analysis)
    }.onFailure {
        Logger.w("Failed to unbind selfie camera", it)
    }
}

private fun captureSelfiePhoto(
    shutterFeedback: MediaActionSound,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    onSuccess: (PlatformFile) -> Unit,
    onFailure: (Throwable) -> Unit,
) {
    val context = App.context
    val outputFile = File.createTempFile("selfie_", ".jpg", context.cacheDir)
    imageCapture.targetRotation = previewView.display?.rotation ?: Surface.ROTATION_0
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                context.performCaptureFeedback(shutterFeedback)
                onSuccess(PlatformFile(uri = Uri.fromFile(outputFile), context = context))
            }

            override fun onError(exception: ImageCaptureException) {
                onFailure(exception)
            }
        },
    )
}

@Composable
private fun SelfieGuidanceCard(messageRes: StringResource) {
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

private fun Context.performCaptureFeedback(
    shutterFeedback: MediaActionSound,
) {
    runCatching {
        shutterFeedback.play(MediaActionSound.SHUTTER_CLICK)
    }.onFailure {
        Logger.w("Failed to play shutter sound", it)
    }

    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    vibrator ?: return
    runCatching {
        vibrator.vibrate(VibrationEffect.createOneShot(28L, VibrationEffect.DEFAULT_AMPLITUDE))
    }.onFailure {
        Logger.w("Failed to vibrate on capture", it)
    }
}

private val SelfieVarietyGoal.mask: Int
    get() = 1 shl ordinal
