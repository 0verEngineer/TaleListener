package org.overengineer.talelistener.di

import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin

fun initKoin(){
    startKoin {
        Napier.d("Starting Koin")
        modules(sharedModule)
    }
}