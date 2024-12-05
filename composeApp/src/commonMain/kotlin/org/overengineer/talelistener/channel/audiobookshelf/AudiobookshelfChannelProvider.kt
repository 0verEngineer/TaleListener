
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.podcast.model
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Removed javax singleton stuff
 * - Use TaleListenerSharedPreferences
 */

package org.overengineer.talelistener.channel.audiobookshelf

import org.overengineer.talelistener.channel.audiobookshelf.common.UnknownAudiobookshelfChannel
import org.overengineer.talelistener.channel.audiobookshelf.common.api.AudiobookshelfAuthService
import org.overengineer.talelistener.channel.audiobookshelf.library.LibraryAudiobookshelfChannel
import org.overengineer.talelistener.channel.audiobookshelf.podcast.PodcastAudiobookshelfChannel
import org.overengineer.talelistener.channel.common.ChannelAuthService
import org.overengineer.talelistener.channel.common.ChannelCode
import org.overengineer.talelistener.channel.common.ChannelProvider
import org.overengineer.talelistener.channel.common.LibraryType
import org.overengineer.talelistener.channel.common.MediaChannel
import org.overengineer.talelistener.persistence.preferences.TaleListenerSharedPreferences

class AudiobookshelfChannelProvider (
    private val podcastAudiobookshelfChannel: PodcastAudiobookshelfChannel,
    private val libraryAudiobookshelfChannel: LibraryAudiobookshelfChannel,
    private val unknownAudiobookshelfChannel: UnknownAudiobookshelfChannel,
    private val audiobookshelfAuthService: AudiobookshelfAuthService,
    private val sharedPreferences: TaleListenerSharedPreferences,
) : ChannelProvider {

    override fun provideMediaChannel(): MediaChannel {
        val libraryType = sharedPreferences
            .getPreferredLibrary()
            ?.type
            ?: LibraryType.UNKNOWN

        return when (libraryType) {
            LibraryType.LIBRARY -> libraryAudiobookshelfChannel
            LibraryType.PODCAST -> podcastAudiobookshelfChannel
            LibraryType.UNKNOWN -> unknownAudiobookshelfChannel
        }
    }

    override fun provideChannelAuth(): ChannelAuthService = audiobookshelfAuthService

    override fun getChannelCode(): ChannelCode = ChannelCode.AUDIOBOOKSHELF
}
