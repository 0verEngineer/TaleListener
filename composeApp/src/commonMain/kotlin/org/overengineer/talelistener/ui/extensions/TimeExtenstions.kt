package org.overengineer.talelistener.ui.extensions


fun Int.formatFully(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}

fun Int.formatShortly(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60

    return "${hours}h ${minutes.toString().padStart(2, '0')}m"
}

fun Int.formatLeadingMinutes(): String {
    val minutes = this / 60
    val seconds = this % 60

    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

