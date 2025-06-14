import ComposeApp
import FirebaseCrashlytics
import UIKit

class IOSCrashlyticsProvider: CrashlyticsProvider {
    
    func logUserEmail(email: String) {
        Crashlytics.crashlytics().setCustomValue(email, forKey: "email")
    }
    
    func logUserId(id: String) {
        Crashlytics.crashlytics().setUserID(id)
    }

}
