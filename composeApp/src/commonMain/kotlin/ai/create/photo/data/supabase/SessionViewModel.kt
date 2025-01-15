package ai.create.photo.data.supabase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

abstract class SessionViewModel : ViewModel() {

    val auth = Supabase.supabase.auth

    var userId: String? = null

    fun loadSession() = viewModelScope.launch {
        Logger.i("loadSession from ${this@SessionViewModel}")
        try {
            auth.sessionStatus.collect {
                Logger.i("Auth status: $it")
                when (it) {
                    is SessionStatus.Authenticated -> {
                        val userId =
                            it.session.user?.id ?: throw IllegalStateException("User id is null")
                        Logger.i("userId: $userId")
                        this@SessionViewModel.userId = userId
                        onAuthenticated()
                    }

                    is SessionStatus.NotAuthenticated -> SupabaseAuth.signInAnonymously()
                    is SessionStatus.Initializing -> onAuthInitializing()
                    is SessionStatus.RefreshFailure -> {
                        val cause = it.cause
                        when (cause) {
                            is RefreshFailureCause.NetworkError -> throw cause.exception
                            is RefreshFailureCause.InternalServerError -> throw cause.exception
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("Sign in failed", e)
            onAuthError(e)
        }
    }


    abstract fun onAuthInitializing()

    abstract fun onAuthenticated()

    abstract fun onAuthError(error: Throwable)
}