package ai.create.photo.ui.gallery.uploads

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.core.PlatformFiles
import org.jetbrains.compose.resources.StringResource
import photocreateai.composeapp.generated.resources.Res
import photocreateai.composeapp.generated.resources.selfie_guidance_brighter_light
import photocreateai.composeapp.generated.resources.selfie_guidance_center_face
import photocreateai.composeapp.generated.resources.selfie_guidance_move_closer
import photocreateai.composeapp.generated.resources.selfie_guidance_multiple_faces
import photocreateai.composeapp.generated.resources.selfie_guidance_no_face
import photocreateai.composeapp.generated.resources.selfie_guidance_ready
import photocreateai.composeapp.generated.resources.selfie_variety_close_up
import photocreateai.composeapp.generated.resources.selfie_variety_face_forward
import photocreateai.composeapp.generated.resources.selfie_variety_laugh
import photocreateai.composeapp.generated.resources.selfie_variety_left_side
import photocreateai.composeapp.generated.resources.selfie_variety_look_down
import photocreateai.composeapp.generated.resources.selfie_variety_look_up
import photocreateai.composeapp.generated.resources.selfie_variety_more_variety
import photocreateai.composeapp.generated.resources.selfie_variety_right_side
import photocreateai.composeapp.generated.resources.selfie_variety_serious
import photocreateai.composeapp.generated.resources.selfie_variety_smile
import photocreateai.composeapp.generated.resources.selfie_variety_upper_body

enum class SelfieGuidanceMessage {
    NO_FACE,
    MULTIPLE_FACES,
    MOVE_CLOSER,
    CENTER_FACE,
    BRIGHTER_LIGHT,
    READY,
}

data class SelfieFrameAssessment(
    val faceCount: Int,
    val largestFaceRatio: Float,
    val faceOffsetX: Float,
    val faceOffsetY: Float,
    val brightness: Float,
    val headPitch: Float,
    val headYaw: Float,
    val headRoll: Float,
    val smileProbability: Float?,
)

data class SelfieGuidance(
    val message: SelfieGuidanceMessage,
    val canCapture: Boolean,
)

enum class SelfieVarietyGoal {
    SMILE,
    SERIOUS,
    LAUGH,
    FACE_FORWARD,
    LOOK_UP,
    LOOK_DOWN,
    LEFT_SIDE,
    RIGHT_SIDE,
    CLOSE_UP,
    UPPER_BODY,
    MORE_VARIETY,
}

fun assessSelfieFrame(frame: SelfieFrameAssessment?): SelfieGuidance {
    if (frame == null || frame.faceCount == 0) {
        return SelfieGuidance(SelfieGuidanceMessage.NO_FACE, canCapture = false)
    }
    if (frame.faceCount > 1) {
        return SelfieGuidance(SelfieGuidanceMessage.MULTIPLE_FACES, canCapture = false)
    }
    if (frame.largestFaceRatio < 0.12f) {
        return SelfieGuidance(SelfieGuidanceMessage.MOVE_CLOSER, canCapture = false)
    }
    val horizontalCenterTolerance = when {
        kotlin.math.abs(frame.headYaw) >= 18f -> 0.40f
        kotlin.math.abs(frame.headYaw) >= 9f -> 0.33f
        else -> 0.26f
    }
    if (kotlin.math.abs(frame.faceOffsetX) > horizontalCenterTolerance ||
        kotlin.math.abs(frame.faceOffsetY) > 0.34f
    ) {
        return SelfieGuidance(SelfieGuidanceMessage.CENTER_FACE, canCapture = false)
    }
    if (frame.brightness < 55f) {
        return SelfieGuidance(SelfieGuidanceMessage.BRIGHTER_LIGHT, canCapture = false)
    }
    return SelfieGuidance(SelfieGuidanceMessage.READY, canCapture = true)
}

fun SelfieGuidanceMessage.labelRes(): StringResource = when (this) {
    SelfieGuidanceMessage.NO_FACE -> Res.string.selfie_guidance_no_face
    SelfieGuidanceMessage.MULTIPLE_FACES -> Res.string.selfie_guidance_multiple_faces
    SelfieGuidanceMessage.MOVE_CLOSER -> Res.string.selfie_guidance_move_closer
    SelfieGuidanceMessage.CENTER_FACE -> Res.string.selfie_guidance_center_face
    SelfieGuidanceMessage.BRIGHTER_LIGHT -> Res.string.selfie_guidance_brighter_light
    SelfieGuidanceMessage.READY -> Res.string.selfie_guidance_ready
}

fun classifySelfieVariety(frame: SelfieFrameAssessment?): Set<SelfieVarietyGoal> {
    if (frame == null || frame.faceCount != 1) return emptySet()
    val goals = linkedSetOf<SelfieVarietyGoal>()
    val isStrongSidePose = kotlin.math.abs(frame.headYaw) >= 20f
    when {
        frame.smileProbability != null && frame.smileProbability >= 0.82f -> goals += SelfieVarietyGoal.LAUGH
        frame.smileProbability != null && frame.smileProbability >= 0.5f -> goals += SelfieVarietyGoal.SMILE
        frame.smileProbability != null &&
                frame.smileProbability <= 0.08f &&
                kotlin.math.abs(frame.headYaw) <= 10f &&
                kotlin.math.abs(frame.headPitch) <= 10f &&
                kotlin.math.abs(frame.headRoll) <= 8f -> {
            goals += SelfieVarietyGoal.SERIOUS
        }
    }
    if (!isStrongSidePose && kotlin.math.abs(frame.headRoll) <= 16f) {
        goals += SelfieVarietyGoal.FACE_FORWARD
    }
    if (frame.headPitch >= 8f && kotlin.math.abs(frame.headYaw) <= 18f) {
        goals += SelfieVarietyGoal.LOOK_UP
    }
    if (frame.headPitch <= -8f && kotlin.math.abs(frame.headYaw) <= 18f) {
        goals += SelfieVarietyGoal.LOOK_DOWN
    }
    if (frame.headYaw <= -16f) {
        goals += SelfieVarietyGoal.LEFT_SIDE
    }
    if (frame.headYaw >= 16f) {
        goals += SelfieVarietyGoal.RIGHT_SIDE
    }
    when {
        frame.largestFaceRatio >= 0.27f -> goals += SelfieVarietyGoal.CLOSE_UP
        frame.largestFaceRatio in 0.12f..0.185f -> goals += SelfieVarietyGoal.UPPER_BODY
    }
    return goals
}

fun nextSelfieVarietyGoal(completed: Set<SelfieVarietyGoal>): SelfieVarietyGoal =
    requiredSelfieVarietyGoals().firstOrNull { it !in completed } ?: SelfieVarietyGoal.MORE_VARIETY

fun requiredSelfieVarietyGoals(): List<SelfieVarietyGoal> = listOf(
    SelfieVarietyGoal.FACE_FORWARD,
    SelfieVarietyGoal.LOOK_UP,
    SelfieVarietyGoal.LOOK_DOWN,
    SelfieVarietyGoal.SMILE,
    SelfieVarietyGoal.SERIOUS,
    SelfieVarietyGoal.CLOSE_UP,
    SelfieVarietyGoal.LAUGH,
    SelfieVarietyGoal.LEFT_SIDE,
    SelfieVarietyGoal.RIGHT_SIDE,
    SelfieVarietyGoal.UPPER_BODY,
)

fun SelfieVarietyGoal.chipLabelRes(): StringResource = when (this) {
    SelfieVarietyGoal.SMILE -> Res.string.selfie_variety_smile
    SelfieVarietyGoal.SERIOUS -> Res.string.selfie_variety_serious
    SelfieVarietyGoal.LAUGH -> Res.string.selfie_variety_laugh
    SelfieVarietyGoal.FACE_FORWARD -> Res.string.selfie_variety_face_forward
    SelfieVarietyGoal.LOOK_UP -> Res.string.selfie_variety_look_up
    SelfieVarietyGoal.LOOK_DOWN -> Res.string.selfie_variety_look_down
    SelfieVarietyGoal.LEFT_SIDE -> Res.string.selfie_variety_left_side
    SelfieVarietyGoal.RIGHT_SIDE -> Res.string.selfie_variety_right_side
    SelfieVarietyGoal.CLOSE_UP -> Res.string.selfie_variety_close_up
    SelfieVarietyGoal.UPPER_BODY -> Res.string.selfie_variety_upper_body
    SelfieVarietyGoal.MORE_VARIETY -> Res.string.selfie_variety_more_variety
}

expect fun supportsSelfieCameraCapture(): Boolean

@Composable
expect fun SelfieCameraCapture(
    uploadedCount: Int,
    targetCount: Int,
    onDismiss: () -> Unit,
    onPhotosCaptured: (PlatformFiles) -> Unit,
    onError: (Throwable) -> Unit,
)
