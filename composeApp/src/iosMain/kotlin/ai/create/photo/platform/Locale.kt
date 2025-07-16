package ai.create.photo.platform

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun getLocale(): String? {
    return NSLocale.currentLocale.languageCode
} 