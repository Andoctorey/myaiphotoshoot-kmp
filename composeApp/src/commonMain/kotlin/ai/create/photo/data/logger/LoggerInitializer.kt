package ai.create.photo.data.logger

import ai.create.photo.platform.platform
import co.touchlab.kermit.Logger

object LoggerInitializer {
    fun initLogger() {
        Logger.setTag(platform().name)
        Logger.addLogWriter(SlackLogWriter())
    }
}