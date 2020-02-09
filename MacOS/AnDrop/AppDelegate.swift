import Cocoa

@NSApplicationMain
class AppDelegate: NSObject, NSApplicationDelegate, FilePropositionPromptDelegate {
    var notificationDelegate: NotificationDelegate?
    
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
        statusItem.image = icon
        statusItem.menu = statusMenu

        let menu = NSMenu()
//        let changeFolderMenuItem = NSMenuItem(title: "Change Download Folder", action: #selector(self.openDownloadFolderChooser), keyEquivalent: "f")
//        changeFolderMenuItem.target = self
//        menu.addItem(changeFolderMenuItem)
        
        let changeAutoAcceptMenuItem = NSMenuItem(title: "Auto Accept Files", action: #selector(self.toggleAutoAccept), keyEquivalent: "a")
        changeAutoAcceptMenuItem.target = self
        if (autoAcceptFiles) {
            changeAutoAcceptMenuItem.state = .on
        } else {
            changeAutoAcceptMenuItem.state = .off
        }
        self.changeAutoAcceptMenuItem = changeAutoAcceptMenuItem
        
        menu.addItem(changeAutoAcceptMenuItem)
        
        menu.addItem(NSMenuItem(title: "Quit Androp", action: #selector(NSApplication.terminate(_:)), keyEquivalent: "q"))
        statusItem.menu = menu
        
        DispatchQueue.main.async {
            let service = NetService(domain: "local.", type: "_androp._tcp.", name: Host.current().localizedName ?? "Unknown MacBook", port: 0)
            service.publish(options: [.listenForConnections])
            service.schedule(in: .main, forMode: RunLoop.Mode.default)
            
            service.delegate = self.serviceDelegate
            print("broadcasting services")
        }
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
        
        let notification = NSUserNotification()
        let failedState = states.first { state in
            switch state {
            case .success(_):
               return false
                
            case .failed(_):
                return true
            }
        }
        
        if (failedState == nil) {
            notification.informativeText = "Success"
            notification.informativeText = "Successfully received files."
            notificationDelegate = NotificationDelegate(isFailed: false, downloadFolder: downloadFolder)

        } else {
            notification.informativeText = "Failed"
            notification.informativeText = "Failed to receive files."
            notificationDelegate = NotificationDelegate(isFailed: true, downloadFolder: downloadFolder)

        }
   
        NSUserNotificationCenter.default.delegate = notificationDelegate
        NSUserNotificationCenter.default.deliver(notification)
    }
}

class NotificationDelegate: NSObject, NSUserNotificationCenterDelegate {
    
    let isFailed: Bool
    let downloadFolder: URL?
    
    init(isFailed: Bool, downloadFolder: URL?) {
        self.isFailed = isFailed
        self.downloadFolder = downloadFolder
    }
    
    func userNotificationCenter(_ center: NSUserNotificationCenter, didActivate notification: NSUserNotification) {
        switch (notification.activationType) {
        case .additionalActionClicked:
            print("Action:")
        case .actionButtonClicked:
            print("Action Button clicked")
        case .contentsClicked:
            if (!isFailed) {
                if let downloadFolder = self.downloadFolder {
                    NSWorkspace.shared.openFile(downloadFolder.absoluteString)
                }
            } else {
                print("Clicked on failed notification")
            }
            
        case .none:
            print("none")
        case .replied:
            print("reply clicked")
        }
    }
}


protocol FilePropositionPromptDelegate {
    func promptUserFileProposition(fileProposition: FileProposition, callback: @escaping (UserAnswer) -> Void)
    func showFileTransferFinishedNotification(states: [FileTransferState])
}
