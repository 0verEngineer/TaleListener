package org.overengineer.talelistener.channel.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.overengineer.talelistener.domain.connection.ServerRequestHeader


class ApiClient(
    serverUrlString: String,
    requestHeaders: List<ServerRequestHeader>?,
    token: String? = null,
    engineFactory: HttpClientEngineFactory<*>
) {

    val httpClient: HttpClient = HttpClient(engineFactory) {
        BrowserUserAgent()

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        val serverUrl = Url(serverUrlString)

        defaultRequest {
            // todo test with a path mapping in url like https://test.audio.com/audiobookshelf
            url {
                host = serverUrl.host
                protocol = serverUrl.protocol
                port = serverUrl.port
                pathSegments = serverUrl.pathSegments
            }

            token?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }

            requestHeaders
                ?.filter { it.name.isNotEmpty() && it.value.isNotEmpty() }
                ?.forEach { header(it.name, it.value) }
        }
    }
}
