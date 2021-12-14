import Cocoa
import UserNotifications

@NSApplicationMain
class AppDelegate: NSObject, NSApplicationDelegate, FilePropositionPromptDelegate {
    
    var downloadFolder: URL? {
        get {
            let downloadFolder: URL?
            
            if let folder = UserDefaults.standard.value(forKey: "folder") as? String {
                downloadFolder = URL(fileURLWithPath: folder)
            } else {
                downloadFolder = FileManager.default.urls(for: .downloadsDirectory, in: .userDomainMask).first
            }
            
            return downloadFolder
        }
        set(newValue) {
            UserDefaults.standard.setValue(newValue?.absoluteString, forKey: "folder")
        }
    }
    
    var autoAcceptFiles: Bool {
        get {
            return UserDefaults.standard.value(forKey: "autoAcceptFiles") as? Bool ?? false
        }
        set(newValue) {
            UserDefaults.standard.setValue(newValue, forKey: "autoAcceptFiles")
        }
    }
    
    @IBOutlet weak var statusMenu: NSMenu!
    let statusItem = NSStatusBar.system.statusItem(withLength:NSStatusItem.variableLength)

    var serviceDelegate : Connection?
    var changeAutoAcceptMenuItem : NSMenuItem? = nil
    
    func applicationDidFinishLaunching(_ aNotification: Notification) {

        serviceDelegate = Connection(filePropositionDelegate: self)

        let icon = NSImage(named: "statusIcon")
        icon?.isTemplate = true
        statusItem.button?.image = icon
        statusItem.menu = statusMenu

        let menu = NSMenu()
//        let changeFolderMenuItem = NSMenuItem(title: "Change Download Folder", action: #selector(self.openDownloadFolderChooser), keyEquivalent: "f")
//        changeFolderMenuItem.target = self
//        menu.addItem(changeFolderMenuItem)
        
        let changeAutoAcceptMenuItem = NSMenuItem(title: NSLocalizedString("menu_item_auto_accept", comment: ""), action: #selector(self.toggleAutoAccept), keyEquivalent: "a")
        changeAutoAcceptMenuItem.target = self
        if (autoAcceptFiles) {
            changeAutoAcceptMenuItem.state = .on
        } else {
            changeAutoAcceptMenuItem.state = .off
        }
        self.changeAutoAcceptMenuItem = changeAutoAcceptMenuItem
        
        menu.addItem(changeAutoAcceptMenuItem)
        
        menu.addItem(NSMenuItem(title: NSLocalizedString("menu_item_quit", comment: ""), action: #selector(NSApplication.terminate(_:)), keyEquivalent: "q"))
        statusItem.menu = menu
        
        DispatchQueue.main.async {
        
            let service = NetService(domain: "local.", type: "_androp._tcp.", name: Host.current().localizedName ?? NSLocalizedString("client_name_fallback", comment: ""), port: 0)
            service.publish(options: [.listenForConnections])
            service.schedule(in: .main, forMode: RunLoop.Mode.default)
            
            service.delegate = self.serviceDelegate
            print("broadcasting services")
        }
        UNUserNotificationCenter.current().delegate = self

    }
    
    @objc func openDownloadFolderChooser(send: AnyObject?) {
        let openPanel = NSOpenPanel()
        openPanel.directoryURL = downloadFolder
        openPanel.allowsMultipleSelection = false
        openPanel.canChooseDirectories = true
        openPanel.canChooseFiles = false
        
        openPanel.runModal()
        
        if let url = openPanel.url {
            self.downloadFolder = url
        }
    }
    
    @objc func toggleAutoAccept(send: AnyObject?) {
        autoAcceptFiles = !autoAcceptFiles
        
        if (autoAcceptFiles) {
            changeAutoAcceptMenuItem?.state = .on
        } else {
            changeAutoAcceptMenuItem?.state = .off
        }
    }
    
    func promptUserFileProposition(fileProposition: FileProposition, callback: @escaping (UserAnswer) -> Void) {
        if let button = statusItem.button {
            let popover = NSPopover()
            popover.contentViewController = FilePropositionViewController.freshController(fileProposition: fileProposition) { answer in
                
                popover.performClose(nil)
                
                DispatchQueue.main.async {
                    callback(answer)
                }
            }
            popover.show(relativeTo: button.bounds, of: button, preferredEdge: NSRectEdge.minY)
        }
    }
    
    func showFileTransferFinishedNotification(states: [FileTransferState]) {
        
        let notification = UNMutableNotificationContent()
        let allSuccess = states.allSatisfy { state in
            switch state {
            case .success(_):
               return true
                
            case .failed(_):
                return false
            }
        }
        
        let url : String?
        
        switch states[0] {
        case .success(let fileURL):
            url = fileURL.absoluteString
            
        case .failed(_):
            url = nil
        }
        
        let identifier: String
        if (allSuccess) {
            notification.title = NSLocalizedString("transfer_notification_success_title", comment: "")
            notification.body = NSLocalizedString("transfer_notification_success_body", comment: "")
            notification.targetContentIdentifier = url
        
            identifier = url ?? ""
        } else {
            notification.title = NSLocalizedString("transfer_notification_failed_title", comment: "")
            notification.body = NSLocalizedString("transfer_notification_failed_body", comment: "")
            identifier = ""
        }
        
        let center = UNUserNotificationCenter.current()

        let request = UNNotificationRequest(identifier: identifier, content: notification, trigger: nil)
        center.add(request)
    }
}

protocol FilePropositionPromptDelegate {
    func promptUserFileProposition(fileProposition: FileProposition, callback: @escaping (UserAnswer) -> Void)
    func showFileTransferFinishedNotification(states: [FileTransferState])
}

extension AppDelegate: UNUserNotificationCenterDelegate {

    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {

        let identifier = response.notification.request.identifier
        let actionIdentifier = response.actionIdentifier

        switch (actionIdentifier) {
        case UNNotificationDefaultActionIdentifier:
            print(identifier)
            let url = URL(string: identifier)
            if let url = url {
                NSWorkspace.shared.activateFileViewerSelecting([url])
            }
        default:
            break;
        }
        completionHandler()
    }
}
