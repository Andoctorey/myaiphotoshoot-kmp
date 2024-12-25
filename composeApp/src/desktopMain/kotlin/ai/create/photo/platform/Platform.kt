package ai.create.photo.platform

import ai.create.photo.data.platform.Platform
import ai.create.photo.data.platform.Platforms


class DesktopPlatform : Platform {
    override val platform = Platforms.DESKTOP
    override val name: String =
        "Desktop ${System.getProperty("os.name")} ${System.getProperty("os.version")}"
}

actual fun platform(): Platform = DesktopPlatform()