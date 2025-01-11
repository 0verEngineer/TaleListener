
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.ui.components
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Migration to coil3
 * - Removed ImageRequest as parameter and build it here
 * - Added host, token, customHeaders and itemId params
 * - Changed coil logic to actually load the image from the server
 */

package org.overengineer.talelistener.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.valentinilk.shimmer.shimmer
import io.ktor.http.URLBuilder
import io.ktor.http.path
import org.overengineer.talelistener.domain.connection.ServerRequestHeader

@Composable
fun AsyncShimmeringImage(
    host: String,
    token: String,
    itemId: String,
    customHeaders: List<ServerRequestHeader>,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
    error: Painter,
    onLoadingStateChanged: (Boolean) -> Unit = {},
) {
    var isLoading by remember { mutableStateOf(true) }
    onLoadingStateChanged(isLoading)

    val context = LocalPlatformContext.current

    val imageRequest = remember(itemId, customHeaders, host, token) {
        val headerBuilder = NetworkHeaders.Builder()
        customHeaders.forEach {
            headerBuilder.add(it.name, it.value)
        }
        headerBuilder.add("Authorization", "Bearer $token")

        ImageRequest.Builder(context)
            .data(URLBuilder(host).apply {
                path("api", "items", itemId, "cover")
            }.build().toString())
            .crossfade(true)
            .httpHeaders(headerBuilder.build())
            .build()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray)
                    .shimmer(),
            )
        }

        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
            onSuccess = {
                isLoading = false
                onLoadingStateChanged(false)
            },
            onError = {
                isLoading = false
                onLoadingStateChanged(false)
            },
            error = error,
        )
    }
}
