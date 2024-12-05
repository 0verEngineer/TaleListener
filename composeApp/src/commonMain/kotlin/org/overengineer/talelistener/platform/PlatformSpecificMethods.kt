package org.overengineer.talelistener.platform

import io.ktor.client.engine.HttpClientEngineFactory

expect fun randomUUID(): String

expect fun getHttpClientEngineFactory(): HttpClientEngineFactory<*>

