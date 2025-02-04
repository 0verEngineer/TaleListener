
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.playback.service
 * Modifications:
 * - Updated package statement
 */

package org.overengineer.talelistener.playback.service

class MimeTypeProvider {

    companion object {
        fun getSupportedMimeTypes() = listOf(
            "audio/flac",
            "audio/mp4",
            "audio/aac",
            "audio/mpeg",
            "audio/mp3",
            "audio/webm",
            "audio/ac3",
            "audio/opus",
            "audio/vorbis",
        )
    }
}
