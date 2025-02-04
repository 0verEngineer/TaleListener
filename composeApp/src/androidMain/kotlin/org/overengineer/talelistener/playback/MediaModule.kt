package org.overengineer.talelistener.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import org.overengineer.talelistener.MainActivity


@OptIn(UnstableApi::class)
fun createExoPlayer(context: Context): ExoPlayer {
    return ExoPlayer.Builder(context)
        .setSeekBackIncrementMs(10_000)
        .setSeekForwardIncrementMs(30_000)
        .setHandleAudioBecomingNoisy(true)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true,
        )
        .build()
}

fun createMediaSession(
    context: Context,
    exoPlayer: ExoPlayer,
): MediaSession {
    val sessionActivityPendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    return MediaSession.Builder(context, exoPlayer)
        .setSessionActivity(sessionActivityPendingIntent)
        .build()
}