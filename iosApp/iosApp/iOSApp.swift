import SwiftUI

@main
struct iOSApp: App {
    NapierProxyKt.debugBuild()

    init() {
        KoinInitializerKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}