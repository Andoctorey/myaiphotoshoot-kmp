package ai.create.photo.app

import ai.create.photo.platform.firebaseOptions
import androidx.multidex.MultiDexApplication
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this, firebaseOptions)
    }

}