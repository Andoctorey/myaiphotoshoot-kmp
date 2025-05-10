import SwiftUI
import FirebaseCore
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    FirebaseApp.configure()
    InAppPurchaseManager.setup()
    return true
  }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        TopUpKt.topUpProvider = IOSTopUpProvider()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea()
        }
    }
}
