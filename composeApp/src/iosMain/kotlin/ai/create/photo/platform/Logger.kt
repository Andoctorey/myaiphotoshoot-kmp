@file:Suppress("unused")

package ai.create.photo.platform

import co.touchlab.kermit.Logger

fun log(message: String) {
    Logger.i(message)
}

fun error(message: String) {
    Logger.e(message)
}