package com.bolt.diy.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Ktor HTTP client configured for bolt.diy API calls.
 */
object BoltHttpClient {
  val instance: HttpClient by lazy {
    HttpClient {
      install(ContentNegotiation) {
        json(Json {
          ignoreUnknownKeys = true
          isLenient = true
          encodeDefaults = true
        })
      }

      install(DefaultRequest) {
        header("Content-Type", "application/json")
      }

      install(HttpTimeout) {
        connectTimeoutMillis = 30_000
        socketTimeoutMillis = 60_000
      }

      expectSuccess = true
    }
  }
}
