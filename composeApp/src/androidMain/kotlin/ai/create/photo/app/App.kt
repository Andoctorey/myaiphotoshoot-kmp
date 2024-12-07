package ai.create.photo.app

import androidx.multidex.MultiDexApplication
import co.touchlab.kermit.Logger

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Logger.i("App onCreate")
    }

}