import Cocoa

class FilePropositionViewController: NSViewController {

    @IBOutlet weak var hostNameLabel: NSTextField!
    @IBOutlet weak var fileNameLabel: NSTextField!
    @IBOutlet weak var fileSizeLabel: NSTextField!
    
    var fileProposition: FileProposition?
    var callback: ((UserAnswer) -> Void)?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        hostNameLabel.stringValue = fileProposition?.hostName ?? NSLocalizedString("unknown_fallback", comment: "")
        
        let fileName: String
        if (fileProposition?.files.count == 1) {
            fileName = fileProposition?.files[0].filename ?? NSLocalizedString("unknown_fallback", comment: "")
        } else {
            let fileCount = fileProposition?.files.count ?? 0
            fileName = String(format: NSLocalizedString("file_count_hint", comment: ""), fileCount)
        }
        
        fileNameLabel.stringValue = fileName
        
        if let files = fileProposition?.files {
            let totalSize = files.map { $0.fileLength }.reduce(0, +)
            let readableFileSize = ByteCountFormatter.string(fromByteCount: totalSize, countStyle: .file)
            fileSizeLabel.stringValue = readableFileSize
        } else {
            fileSizeLabel.stringValue = NSLocalizedString("unknown_fallback", comment: "")
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
        let storyboard = NSStoryboard(name: "Main", bundle: nil)
        let identifier = "FilePropositionViewController"
        guard let viewcontroller = storyboard.instantiateController(withIdentifier: identifier) as? FilePropositionViewController else {
            fatalError("Why cant i find FilePropositionViewController?")
        }
        
        viewcontroller.fileProposition = fileProposition
        viewcontroller.callback = callback
        
        return viewcontroller
    }
}
