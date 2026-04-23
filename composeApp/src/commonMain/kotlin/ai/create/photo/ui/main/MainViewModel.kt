package ai.create.photo.ui.main

import ai.create.photo.data.supabase.SupabaseFunction
import ai.create.photo.data.supabase.database.ProfilesRepository
import ai.create.photo.data.supabase.isInsufficientFundsError
import ai.create.photo.platform.topUpPlatform
import ai.create.photo.platform.updateGenerationProgress
import ai.create.photo.ui.auth.AuthViewModel
import ai.create.photo.ui.generate.Prompt
import ai.create.photo.ui.settings.balance.Pricing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainViewModel : AuthViewModel() {

    private data class PendingGenerationRequest(
        val trainingId: String,
        val prompt: String,
        val parentGenerationId: String?,
        val photosToGenerate: Int,
    )

    private var pendingGenerationRequest: PendingGenerationRequest? = null

    var uiState by mutableStateOf(MainUiState())
        private set


    override fun onAuthInitializing() {}

    override fun onAuthenticated(userChanged: Boolean) {}

    override fun onAuthError(error: Throwable) {}

    fun generatePhoto(
        trainingId: String,
        prompt: String,
        parentGenerationId: String?,
        photosToGenerate: Int
    ) = viewModelScope.launch {
        val request = PendingGenerationRequest(
            trainingId = trainingId,
            prompt = prompt,
            parentGenerationId = parentGenerationId,
            photosToGenerate = photosToGenerate,
        )

        pendingGenerationRequest = request
        val insufficientFundsError = loadInsufficientFundsError()
        if (insufficientFundsError != null) {
            uiState = uiState.copy(errorPopup = null, topUpErrorPopup = insufficientFundsError)
            return@launch
        }

        pendingGenerationRequest = null
        updateGenerationsInProgress(uiState.generationsInProgress + photosToGenerate)
        try {
            repeat(photosToGenerate) {
                launch {
                    try {
                        SupabaseFunction.generatePhoto(trainingId, prompt, parentGenerationId)
                    } catch (e: Exception) {
                        currentCoroutineContext().ensureActive()
                        if (isAuthenticated) {
                            Logger.e("Generate photo failed", e)
                            uiState = if (e.isInsufficientFundsError()) {
                                queuePendingGenerationRetry(request)
                                uiState.copy(errorPopup = null, topUpErrorPopup = e)
                            } else {
                                uiState.copy(errorPopup = e, topUpErrorPopup = null)
                            }
                        }
                    } finally {
                        updateGenerationsInProgress(uiState.generationsInProgress - 1)
                    }
                }
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            if (isAuthenticated) Logger.e("Generate photo failed", e)
            updateGenerationsInProgress(uiState.generationsInProgress - 1)
        }
    }

    fun topUp() = viewModelScope.launch {
        val userId = user?.id ?: return@launch
        topUpPlatform(
            userId = userId,
            pricing = Pricing.MAIN,
            onFailure = {
                uiState = uiState.copy(errorPopup = it, topUpErrorPopup = null)
            },
            onSuccess = {
                viewModelScope.launch {
                    repeat(10) {
                        try {
                            val profile = ProfilesRepository.loadProfile(userId)
                            if (profile != null && profile.balance + BALANCE_EPSILON >= PHOTO_COST_USD) {
                                val pendingRequest = pendingGenerationRequest
                                uiState = uiState.copy(showBalanceUpdatedPopup = true)
                                pendingRequest?.let {
                                    generatePhoto(
                                        trainingId = it.trainingId,
                                        prompt = it.prompt,
                                        parentGenerationId = it.parentGenerationId,
                                        photosToGenerate = it.photosToGenerate,
                                    )
                                }
                                return@launch
                            }
                        } catch (e: Exception) {
                            currentCoroutineContext().ensureActive()
                            if (!isAuthenticated) return@launch
                            Logger.w("Polling profile after top-up failed, will retry", e)
                        }
                        delay(5000L)
                    }
                }
            },
        )
    }

    private fun updateGenerationsInProgress(progress: Int) {
        uiState = uiState.copy(generationsInProgress = progress)
        updateGenerationProgress(progress)
    }

    fun toggleOpenCreations(openCreations: Boolean) {
        uiState = uiState.copy(openCreations = openCreations)
    }

    fun hideErrorPopup() {
        if (uiState.topUpErrorPopup != null) {
            pendingGenerationRequest = null
        }
        uiState = uiState.copy(errorPopup = null, topUpErrorPopup = null)
    }

    fun hideBalanceUpdatedPopup() {
        uiState = uiState.copy(showBalanceUpdatedPopup = false)
    }

    fun toggleOpenUploads(openUploads: Boolean, showUploadHint: Boolean = false) {
        uiState = uiState.copy(
            openUploads = openUploads,
            showUploadHint = if (openUploads) showUploadHint else false,
        )
    }

    fun putPrompt(prompt: Prompt?) {
        uiState = uiState.copy(putPrompt = prompt)
    }

    fun toggleResetSettingTab(reset: Boolean) {
        uiState = uiState.copy(resetSettingTab = reset)
    }

    private suspend fun loadInsufficientFundsError(): IllegalStateException? {
        val userId = user?.id ?: return null
        return try {
            val profile = ProfilesRepository.loadProfile(userId) ?: return null
            if (profile.balance + BALANCE_EPSILON >= PHOTO_COST_USD) return null
            IllegalStateException(
                "Insufficient funds. Required: $${photoCostUsd()}. Available: $${profile.formattedBalance}"
            )
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            if (isAuthenticated) {
                Logger.w(
                    "loadProfile before generation failed, proceeding without balance precheck",
                    e
                )
            }
            null
        }
    }

    private fun photoCostUsd(): String {
        val cents = (PHOTO_COST_USD * 100).roundToInt()
        val dollars = cents / 100
        val fraction = cents % 100
        return if (fraction == 0) {
            dollars.toString()
        } else {
            val padded = if (fraction < 10) "0$fraction" else fraction.toString()
            "$dollars.${padded.trimEnd('0')}"
        }
    }

    private fun queuePendingGenerationRetry(request: PendingGenerationRequest) {
        val currentRequest = pendingGenerationRequest
        pendingGenerationRequest = if (
            currentRequest != null &&
            currentRequest.trainingId == request.trainingId &&
            currentRequest.prompt == request.prompt &&
            currentRequest.parentGenerationId == request.parentGenerationId
        ) {
            currentRequest.copy(photosToGenerate = currentRequest.photosToGenerate + 1)
        } else {
            request.copy(photosToGenerate = 1)
        }
    }

    companion object {
        private const val PHOTO_COST_USD = 0.03f
        private const val BALANCE_EPSILON = 0.0001f
    }
}
