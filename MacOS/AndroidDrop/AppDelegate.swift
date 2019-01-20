//
//  AppDelegate.swift
//  AndroidDrop
//
//  Created by Can Yumusak on 07.06.18.
//  Copyright © 2018 Can Yumusak. All rights reserved.
//

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

        let icon = NSImage(named: NSImage.Name("statusIcon"))
        icon?.isTemplate = true
        statusItem.image = icon
        statusItem.menu = statusMenu

        let menu = NSMenu()
        let changeFolderMenuItem = NSMenuItem(title: "Change Download Folder", action: #selector(self.openDownloadFolderChooser), keyEquivalent: "f")
        changeFolderMenuItem.target = self
        menu.addItem(changeFolderMenuItem)
        
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
            service.schedule(in: .main, forMode: .defaultRunLoopMode)
            
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
    
    func showFileTransferFinishedNotification(state: FileTransferState) {
        
        let notification = NSUserNotification()
        
        switch state {
        case .success(_):
            notification.informativeText = "Success"
            
        case .failed(_):
            notification.informativeText = "Failed)"
        }
        
        switch state {
        case .success(let url):
            notification.informativeText = "Successfully received file \(url.lastPathComponent).)"
            
        case .failed(let filename):
            notification.informativeText = "Failed to receive \(filename).)"
        }
        notificationDelegate = NotificationDelegate(state: state)
        NSUserNotificationCenter.default.delegate = notificationDelegate
        NSUserNotificationCenter.default.deliver(notification)
    }
}

class NotificationDelegate: NSObject, NSUserNotificationCenterDelegate {
    
    let state: FileTransferState
    
    init(state: FileTransferState) {
        self.state = state
    }
    
    func userNotificationCenter(_ center: NSUserNotificationCenter, didActivate notification: NSUserNotification) {
        switch (notification.activationType) {
        case .additionalActionClicked:
            print("Action:")
        case .actionButtonClicked:
            print("Action Button clicked")
        case .contentsClicked:
            switch state {
            case .success(let url):
                NSWorkspace.shared.openFile(url.absoluteString)
            case .failed(_):
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
    func showFileTransferFinishedNotification(state: FileTransferState)
}
