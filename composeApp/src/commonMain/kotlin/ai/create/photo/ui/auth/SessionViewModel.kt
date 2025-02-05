package ai.create.photo.ui.auth

import ai.create.photo.data.supabase.Supabase.supabase
import ai.create.photo.data.supabase.SupabaseAuth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

abstract class SessionViewModel : ViewModel() {

    val auth = supabase.auth

    var user: User? = null

    fun loadSession() = viewModelScope.launch {
        Logger.i("loadSession from ${this@SessionViewModel}")
        try {
            auth.sessionStatus.collect {
                Logger.i("Auth status: $it")
                when (it) {
                    is SessionStatus.Authenticated -> {
                        val supabaseUser = it.session.user
                        val emailChanged = user?.id != supabaseUser?.id
                        loadUser()
                        onAuthenticated(emailChanged)
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
            currentCoroutineContext().ensureActive()
            Logger.e("Sign in failed", e)
            onAuthError(e)
        }
    }

    fun loadUser() {
        val sessionStatus = supabase.auth.sessionStatus.value
        if (sessionStatus !is SessionStatus.Authenticated) return
        val supabaseUser = sessionStatus.session.user
        val user = User(
            id = supabaseUser?.id ?: throw IllegalStateException("User id is null"),
            email = supabaseUser.email,
            balance = 0f,
        )
        Logger.i("user: $user")
        this@SessionViewModel.user = user
    }

    abstract fun onAuthInitializing()

    abstract fun onAuthenticated(userChanged: Boolean)

    abstract fun onAuthError(error: Throwable)
}