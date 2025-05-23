import ComposeApp
import UIKit

class IOSOpenUrlProvider: OpenUrlProvider {
    
    func openUrl(url: String) {
        if let url = URL(string: url) {
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
    }

}
