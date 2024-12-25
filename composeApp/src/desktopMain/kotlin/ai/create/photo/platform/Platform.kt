package ai.create.photo.platform


class DesktopPlatform : Platform {
    override val name: String =
        "Desktop (JVM) ${System.getProperty("os.name")} ${System.getProperty("os.version")}"
}

actual fun platform(): Platform = DesktopPlatform()