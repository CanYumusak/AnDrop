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
        
        let fileName: String
        if (fileProposition?.files.count == 1) {
            fileName = fileProposition?.files[0].filename ?? "Unknown"
        } else {
            fileName = "\(fileProposition?.files.count ?? 0) files"
        }
        
        fileNameLabel.stringValue = fileName
        
        if let files = fileProposition?.files {
            let totalSize = files.map { $0.fileLength }.reduce(0, +)
            let readableFileSize = ByteCountFormatter.string(fromByteCount: totalSize, countStyle: .file)
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
