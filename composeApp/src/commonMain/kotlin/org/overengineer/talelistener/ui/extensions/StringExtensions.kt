package org.overengineer.talelistener.ui.extensions

import kotlin.math.pow

fun Float.toFixed(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val rounded = kotlin.math.round(this * factor) / factor
    return rounded.toString()
}