//
//  FilePropositionViewController.swift
//  AndroidDrop
//
//  Created by Can Yumusak on 17.10.18.
//  Copyright © 2018 Can Yumusak. All rights reserved.
//

import Cocoa

class FilePropositionViewController: NSViewController {

    @IBOutlet weak var hostNameLabel: NSTextField!
    @IBOutlet weak var fileNameLabel: NSTextField!
    @IBOutlet weak var fileSizeLabel: NSTextField!
    
    var fileProposition: FileProposition?
    var callback: ((UserAnswer) -> Void)?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        hostNameLabel.stringValue = fileProposition?.hostName ?? "Unknown"
        fileNameLabel.stringValue = fileProposition?.filename ?? "Unknown"
        
        if let fileLength = fileProposition?.fileLength {
            let readableFileSize = ByteCountFormatter.string(fromByteCount: fileLength, countStyle: .file)
            fileSizeLabel.stringValue = readableFileSize
        } else {
            fileSizeLabel.stringValue = "Unknown"
        }
    }
    
    @IBAction func acceptFile(_ sender: Any) {
        self.callback?(.accept)
    }
    
    @IBAction func denyFile(_ sender: Any) {
        dismiss(sender)
        self.callback?(.deny)
    }
}

extension FilePropositionViewController {
    static func freshController(fileProposition: FileProposition, callback: @escaping (UserAnswer) -> Void) -> FilePropositionViewController {
        let storyboard = NSStoryboard(name: NSStoryboard.Name(rawValue: "Main"), bundle: nil)
        let identifier = NSStoryboard.SceneIdentifier(rawValue: "FilePropositionViewController")
        guard let viewcontroller = storyboard.instantiateController(withIdentifier: identifier) as? FilePropositionViewController else {
            fatalError("Why cant i find FilePropositionViewController?")
        }
        
        viewcontroller.fileProposition = fileProposition
        viewcontroller.callback = callback
        
        return viewcontroller
    }
}
