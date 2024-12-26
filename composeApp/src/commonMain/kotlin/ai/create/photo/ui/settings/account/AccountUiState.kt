package ai.create.photo.ui.settings.account

import androidx.compose.runtime.Immutable

@Immutable
data class AccountUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,

    val email: String = "",
    val isInvalidEmail: Boolean = false,

    val isSendingOtp: Boolean = false,
    val enterOtp: Boolean = false,

    val otp: String = "",
    val isIncorrectOtp: Boolean = false,

    val isVerifyingOtp: Boolean = false,
    val isVerified: Boolean = false
)