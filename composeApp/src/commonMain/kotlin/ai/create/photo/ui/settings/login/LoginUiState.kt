package ai.create.photo.ui.settings.login

import androidx.compose.runtime.Immutable
import io.github.jan.supabase.auth.user.UserInfo

@Immutable
data class LoginUiState(
    val isLoading: Boolean = false,
    val loadingError: Throwable? = null,
    val errorPopup: Throwable? = null,
    val user: UserInfo? = null,

    val email: String = "",
    val isInvalidEmail: Boolean = false,

    val isSendingOtp: Boolean = false,
    val enterOtp: Boolean = false,

    val otp: String = "",
    val isIncorrectOtp: Boolean = false,

    val isVerifyingOtp: Boolean = false,
)