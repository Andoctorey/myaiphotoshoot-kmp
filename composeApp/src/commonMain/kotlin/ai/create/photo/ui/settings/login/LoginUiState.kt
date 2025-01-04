package ai.create.photo.ui.settings.login

import androidx.compose.runtime.Immutable

@Immutable
data class LoginUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,
    val email: String? = null,

    val emailToVerify: String = "",
    val isInvalidEmail: Boolean = false,

    val isSendingOtp: Boolean = false,
    val enterOtp: Boolean = false,

    val otp: String = "",
    val isIncorrectOtp: Boolean = false,

    val isVerifyingOtp: Boolean = false,
)