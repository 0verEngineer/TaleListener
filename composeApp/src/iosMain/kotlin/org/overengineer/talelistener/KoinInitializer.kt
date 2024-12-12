package org.overengineer.talelistener

import org.koin.core.context.startKoin
import org.overengineer.talelistener.di.commonModule
import org.overengineer.talelistener.di.platformModule

fun initKoin() {
    startKoin {
        modules(commonModule(), platformModule())
    }
}