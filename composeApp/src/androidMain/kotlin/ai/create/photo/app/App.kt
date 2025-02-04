package ai.create.photo.app

import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDexApplication
import co.touchlab.kermit.Logger

class App : MultiDexApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        Logger.i("App onCreate")
        context = applicationContext
    }

}