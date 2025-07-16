package ai.create.photo.platform

import ai.create.photo.app.App

actual fun getLocale(): String? = App.context.resources.configuration.locales[0].language