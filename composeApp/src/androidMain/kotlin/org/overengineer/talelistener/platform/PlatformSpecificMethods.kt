package org.overengineer.talelistener.platform

// Android
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import java.util.UUID

actual fun randomUUID() = UUID.randomUUID().toString()

actual fun getHttpClientEngineFactory(): HttpClientEngineFactory<*> {
    return OkHttp
}
