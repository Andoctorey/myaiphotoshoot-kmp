package ai.create.photo.platform

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val platform = Platforms.IOS
    override val name: String =
        "ios" + UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun platform(): Platform = IOSPlatform()