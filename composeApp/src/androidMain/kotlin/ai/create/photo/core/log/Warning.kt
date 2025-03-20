package ai.create.photo.core.log

import androidx.annotation.Keep

@Keep
class Warning(message: String) : Exception(message) {

    init {
        val stackTrace = stackTrace.toMutableList()
        val iterator = stackTrace.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if ("timber" in element.toString()
                    .lowercase() || "FirebaseCrashlyticsTree" in element.toString()
            ) {
                iterator.remove()
            }
        }
        setStackTrace(stackTrace.toTypedArray())
    }
}