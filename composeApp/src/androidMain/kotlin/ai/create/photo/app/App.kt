package ai.create.photo.app

import ai.create.photo.data.logger.FilteredCrashlyticsLogWriter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.multidex.MultiDexApplication
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger

class App : MultiDexApplication() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        @SuppressLint("StaticFieldLeak")
        var currentActivity: Activity? = null
    }

    override fun onCreate() {
        super.onCreate()
        Logger.i("App onCreate")
        context = applicationContext
        registerActivityLifecycleCallbacks()
        initCrashlytics()
    }

    @OptIn(ExperimentalKermitApi::class)
    // https://kermit.touchlab.co/docs/crashreporting/CRASHLYTICS
    private fun initCrashlytics() {
        // Add filtered writer first to suppress expected network noise.
        Logger.addLogWriter(FilteredCrashlyticsLogWriter())
    }

    private fun registerActivityLifecycleCallbacks() =
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = activity
                Logger.i("onActivityCreated: $activity")
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
                Logger.i("onActivityStarted: $activity")
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
                Logger.i("onActivityResumed: $activity")
            }

            override fun onActivityPaused(activity: Activity) {
                Logger.i("onActivityPaused: $activity")
            }

            override fun onActivityStopped(activity: Activity) {
                Logger.i("onActivityStopped: $activity")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Logger.i("onActivitySaveInstanceState: $activity")
            }

            override fun onActivityDestroyed(activity: Activity) {
                Logger.i("onActivityDestroyed: $activity")
                if (currentActivity === activity) {
                    currentActivity = null
                }
            }
        })

}