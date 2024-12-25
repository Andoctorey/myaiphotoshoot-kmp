package ai.create.photo.platform


interface Platform {
    val name: String
}

expect fun platform(): Platform

