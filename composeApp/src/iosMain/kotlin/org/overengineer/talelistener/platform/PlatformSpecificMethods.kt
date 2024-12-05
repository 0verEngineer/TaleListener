package org.overengineer.talelistener.platform

// iOS
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()

actual fun getHttpClientEngineFactory(): HttpClientEngineFactory<*> {
    return Darwin
}
