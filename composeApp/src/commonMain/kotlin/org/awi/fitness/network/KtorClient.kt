package org.awi.fitness.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClient(): HttpClient

object KtorClient {
    val httpClient = createPlatformHttpClient().config {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 30_000 // 30 seconds
            socketTimeoutMillis = 120_000 // 120 seconds for AI generation
            requestTimeoutMillis = 120_000 // 120 seconds
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("HTTP Client: $message")
                }
            }
            level = LogLevel.ALL
            sanitizeHeader { header -> header == "Authorization" }
        }
    }
} 