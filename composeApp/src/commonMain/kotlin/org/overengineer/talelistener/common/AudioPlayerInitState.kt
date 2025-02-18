package org.overengineer.talelistener.common

enum class AudioPlayerInitState {
    SUCCESS,
    WAITING,
    ANDROID_CALLBACK_SETUP_FAIL,
    DESKTOP_VLC_NOT_FOUND
}
