package ai.create.photo.ui.auth

import ai.create.photo.data.runCatchingCancellable
import ai.create.photo.data.supabase.Supabase.supabase
import ai.create.photo.data.supabase.SupabaseAuth
import ai.create.photo.data.supabase.model.User
import ai.create.photo.platform.logUserEmail
import ai.create.photo.platform.logUserId
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

abstract class AuthViewModel : ViewModel() {

    val auth = supabase.auth
    var user: User? = null
    private var lastAuthenticatedUserId: String? = null
    val isAuthenticated
        get() = auth.sessionStatus.value is SessionStatus.Authenticated

    init {
        loadSession()
    }

    companion object {
        var refreshToken: String? = null
    }

    @OptIn(SupabaseExperimental::class)
    fun loadSession() {
        viewModelScope.launch {
            // to init extended classes variables
            delay(1)
            Logger.i("loadSession from ${this@AuthViewModel::class.simpleName}")
            observeAuthEvents()
        }

        viewModelScope.launch {
            // to init extended classes variables
            delay(1)
            observeSessionStatus()
        }
    }

    private suspend fun observeSessionStatus() {
        try {
            auth.sessionStatus.collect { status ->
                if (status !is SessionStatus.Authenticated) {
                    Logger.i("Auth status: $status")
                }
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val supabaseUser = status.session.user
                        refreshToken = status.session.refreshToken
                        val emailChanged =
                            lastAuthenticatedUserId != null && lastAuthenticatedUserId != supabaseUser?.id
                        loadUser()
                        lastAuthenticatedUserId = user?.id
                        onAuthenticated(emailChanged)
                    }

                    is SessionStatus.NotAuthenticated -> {
                        user = null
                        viewModelScope.launch {
                            runCatchingCancellable { SupabaseAuth.signInAnonymously() }
                                .onFailure { error ->
                                    Logger.w(
                                        "Anonymous sign-in error (non-fatal)",
                                        error
                                    )
                                }
                        }
                    }
                    is SessionStatus.Initializing -> onAuthInitializing()
                    is SessionStatus.RefreshFailure -> Unit
                }
            }
        } catch (e: Exception) {
            handleAuthException(e)
        }
    }

    @OptIn(SupabaseExperimental::class)
    private suspend fun observeAuthEvents() {
        try {
            auth.events.collect { event ->
                if (
                    event is AuthEvent.RefreshFailure &&
                    auth.sessionStatus.value is SessionStatus.RefreshFailure
                ) {
                    when (val cause = event.cause) {
                        is RefreshFailureCause.NetworkError -> throw cause.exception
                        is RefreshFailureCause.InternalServerError -> throw cause.exception
                    }
                }
            }
        } catch (e: Exception) {
            handleAuthException(e)
        }
    }

    private suspend fun handleAuthException(e: Exception) {
        currentCoroutineContext().ensureActive()
        Logger.e("Sign in failed", e)
        if (e.message?.contains("JWT expired") == true && refreshToken != null) {
            try {
                supabase.auth.refreshSession(refreshToken!!)
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                Logger.e("Refresh session failed", e)
            }
        }
        onAuthError(e)
    }

    protected suspend fun awaitAuthenticatedUserId(timeoutMs: Long = 8000L): String? {
        val immediateStatus = auth.sessionStatus.value as? SessionStatus.Authenticated
        if (immediateStatus != null) {
            loadUser()
            return immediateStatus.session.user?.id ?: user?.id
        }

        Logger.d("awaitAuthenticatedUserId: waiting for authenticated session")
        val status = withTimeoutOrNull(timeoutMs) {
            auth.sessionStatus.filterIsInstance<SessionStatus.Authenticated>().first()
        } ?: run {
            Logger.w("awaitAuthenticatedUserId timeout after ${timeoutMs}ms; status=${auth.sessionStatus.value}")
            return null
        }

        loadUser()
        return status.session.user?.id ?: user?.id
    }

    fun loadUser() {
        val sessionStatus = supabase.auth.sessionStatus.value
        if (sessionStatus !is SessionStatus.Authenticated) return
        val supabaseUser = sessionStatus.session.user

        val user = User(
            id = supabaseUser?.id ?: throw IllegalStateException("User id is null"),
            email = supabaseUser.email.takeIf { !it.isNullOrEmpty() },
        )
        Logger.i("$user")
        logUserId(user.id)
        user.email?.run { logUserEmail(this) }
        this@AuthViewModel.user = user
    }

    abstract fun onAuthInitializing()

    abstract fun onAuthenticated(userChanged: Boolean)

    abstract fun onAuthError(error: Throwable)
}
