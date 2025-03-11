package ai.create.photo.app

import android.annotation.SuppressLint
import android.content.Context
import androidx.multidex.MultiDexApplication
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter

class App : MultiDexApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        Logger.i("App onCreate")
        context = applicationContext
        initCrashlytics()
    }

    @OptIn(ExperimentalKermitApi::class)
    // https://kermit.touchlab.co/docs/crashreporting/CRASHLYTICS
    private fun initCrashlytics() {
        Logger.addLogWriter(CrashlyticsLogWriter())
    }

}