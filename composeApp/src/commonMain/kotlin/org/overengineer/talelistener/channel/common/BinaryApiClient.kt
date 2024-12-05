package org.overengineer.talelistener.channel.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import org.overengineer.talelistener.domain.connection.ServerRequestHeader

class BinaryApiClient(
    serverUrlString: String,
    requestHeaders: List<ServerRequestHeader>?,
    token: String,
    engineFactory: HttpClientEngineFactory<*>
) {

    val httpClient: HttpClient = HttpClient(engineFactory) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        val serverUrl = Url(serverUrlString)

        defaultRequest {
            url {
                host = serverUrl.host
                protocol = serverUrl.protocol
                port = serverUrl.port
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
