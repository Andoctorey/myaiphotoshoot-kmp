package ai.create.photo.platform

import com.google.firebase.crashlytics.FirebaseCrashlytics

actual fun logUserEmail(email: String) {
    FirebaseCrashlytics.getInstance().setCustomKey("email", email)
}

actual fun logUserId(id: String) {
    FirebaseCrashlytics.getInstance().setUserId(id)
}