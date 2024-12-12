import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        NapierProxyKt.debugBuild()
        KoinInitializerKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
