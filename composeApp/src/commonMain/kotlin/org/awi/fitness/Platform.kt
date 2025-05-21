package org.awi.fitness

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform