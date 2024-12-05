package ai.create.photo.data.firebase.auth

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class Auth {

    private val _userId: MutableStateFlow<String?> = MutableStateFlow(null)
    val userId: StateFlow<String?> = _userId

    init {
        MainScope().launch {
            val user = firebaseAuth() ?: return@launch
            saveUserId(user)
            _userId.value = user.uid
        }
    }

    private suspend fun firebaseAuth(): FirebaseUser? {
        Logger.i("firebaseAuth")
        val auth = Firebase.auth
        val firebaseUser = auth.currentUser // TODO why creates new each time
        if (firebaseUser == null) {
            Logger.i("sign up")
            return auth.signInAnonymously().user
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Logger.i(task.exception, "Sign up successful ${auth.currentUser?.uid}")
//                        continuation.resume(auth.currentUser!!)
//                    } else {
//                        Logger.e(task.exception, "Sign up fail")
//                        context.toast(R.string.main_screen_authentication_failed)
//                        bus.post(CloseAppEvent())
//                    }
//                }

        } else {
            Logger.i("Sign in successful ${auth.currentUser?.uid}")
            return firebaseUser
        }
    }


    private fun saveUserId(user: FirebaseUser) {
        Logger.i("saveUserId")
//        FirebaseCrashlytics.getInstance().apply {
//            setUserId(user.uid)
//            setCustomKey("device_id", context.getPseudoDeviceId())
//        }
//        analytics.setUserId(user.uid)
//        prefs.setUserId(user.uid)
    }
}