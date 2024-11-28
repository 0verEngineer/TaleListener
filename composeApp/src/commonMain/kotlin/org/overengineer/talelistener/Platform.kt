package org.overengineer.talelistener

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform