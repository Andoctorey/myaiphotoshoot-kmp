package ai.create.photo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform