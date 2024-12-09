package org.overengineer.talelistener.di

import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

// Todo: test on ios, this is eventually not needed
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    Napier.d("Starting Koin")
    startKoin {
        appDeclaration()
        modules(commonModule())
    }
}
