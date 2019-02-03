
import Cocoa


enum ReceivedEvent {
    case fileSendRequest(hostName: String, files: [File])
    case invalid
}

enum UserAnswer {
    case accept
    case deny
}

enum FileTransferState {
    case success(url: URL)
    case failed(filename: String)
}

enum ReceivedEventType: String {
    case send_file
}

class Connection: NSObject, NetServiceDelegate, StreamDelegate {
    private let maxLength = 4096
    private var outputStream : OutputStream? = nil
    private var waitingForFile = false
    private var currentlyTransferringFile = false
    private var files : [File]? = nil
    
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
    }
    
    var autoAcceptFiles: Bool {
        get {
            return UserDefaults.standard.value(forKey: "autoAcceptFiles") as? Bool ?? false
        }
        set(newValue) {
            UserDefaults.standard.setValue(newValue, forKey: "autoAcceptFiles")
        }
    }
    
    let filePropositionDelegate: FilePropositionPromptDelegate
    
    init(filePropositionDelegate: FilePropositionPromptDelegate) {
        self.filePropositionDelegate = filePropositionDelegate
    }

    func netServiceDidStop(_ sender: NetService) {
        print("Did stop: \(sender)")
    }
    
    func stream(_ stream: Stream, handle eventCode: Stream.Event) {
        switch (eventCode){
        case .openCompleted:
            print("Stream opened \(stream)")
            
            if let outputStream = stream as? OutputStream {
                print("Sending handshake event")
                self.send(event: HandshakeEvent(), stream: outputStream)
            }
            
        case .hasBytesAvailable:
            print("Received Message")
            DispatchQueue.main.async {
                let inputStream = stream as! InputStream
                self.readAvailableBytes(stream: inputStream)
            }
            
        case .errorOccurred:
            print("ErrorOccurred")
            
        case .endEncountered:
            print("EndEncountered")
            
        default:
            print("unknown.")
        }
    }
    
    func readAvailableBytes(stream: InputStream) {
        guard !waitingForFile else {
            readFile(stream: stream)
            return
        }
        
        DispatchQueue.main.async {
            var data = Data()
            let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: self.maxLength)
            let length = stream.read(buffer, maxLength: self.maxLength)
            data.append(buffer, count: length)
            
            let object = try? JSONSerialization.jsonObject(with: data) as! [String: Any]
            let event = self.parseMessage(message: object)
            
            switch (event) {
            case .invalid:
                print ("received invalid event")
            case .fileSendRequest(let hostName, let files):
                if let outputStream = self.outputStream {
                    self.respondToFileRequest(hostName: hostName, files: files, stream: outputStream)
                }
            }
        }
    }
    
    func readFile(stream: InputStream) {
        guard let filename = self.filename, let fileLength = self.fileLength else {
            print("started file transfer without filename")
            return
        }
        currentlyTransferringFile = true
        
        var data = Data()
        let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: self.maxLength)
        while data.count < fileLength && stream.streamStatus == .open {
            let length = stream.read(buffer, maxLength: self.maxLength)
            data.append(buffer, count: length)
            print("appended \(length), total length is \(data.count) out of \(fileLength)")
        }
        
        let transferState: FileTransferState
        if (data.count >= fileLength) {
            print("file transfer finished")
            if let dir = downloadFolder {
                do {
                    let fileURL = dir.appendingPathComponent(filename)
                    try data.write(to: fileURL)
                    transferState = .success(url: fileURL)
                } catch {
                    print("error while writing file \(error)")
                    transferState = .failed(filename: filename)
                }
            } else {
                transferState = .failed(filename: filename)
            }
            
        } else {
            print("file transfer cancelled")
            transferState = .failed(filename: filename)
        }
        
        waitingForFile = false
        currentlyTransferringFile = false
        filePropositionDelegate.showFileTransferFinishedNotification(state: transferState)
    }
    
    func respondToFileRequest(hostName: String, files: [File], stream: OutputStream) {
        self.files = files
    
        let fileProposition = FileProposition(hostName: hostName, files: files)
        if (autoAcceptFiles) {
            self.waitingForFile = true
            self.send(event: AcceptEvent(), stream: stream)
        } else {
            filePropositionDelegate.promptUserFileProposition(fileProposition: fileProposition) { answer in
                switch answer {
                case .accept:
                    self.send(event: AcceptEvent(), stream: stream)
                    self.waitingForFile = true
                    
                case .deny :
                    self.send(event: DenyEvent(), stream: stream)
                    
                }
                
            }
            
        }
    }
    
    fileprivate func send(event: SentEvent, stream: OutputStream) {
        DispatchQueue.main.async {
            var error:NSError?
            JSONSerialization.writeJSONObject(event.serialize(), to: stream, options: [], error: &error)
            
            self.sendNewline(stream: stream)
            if let error = error {
                print("Failed to write JSON data: \(error.localizedDescription)")
            }
        }
    }
    
    func sendNewline(stream: OutputStream) {
        guard let data = "\r\n".data(using: .utf8) else { return }
        _ = data.withUnsafeBytes { stream.write($0, maxLength: data.count) }
        print("sent newline")
    }
    
    func parseMessage(message: [String: Any]?) -> ReceivedEvent {
        return message.map { ReceivedEvent(rawValue: $0) } ?? .invalid
    }
    
    func netService(_ sender: NetService,
                    didAcceptConnectionWith inputStream: InputStream,
                    outputStream: OutputStream) {
        print("Did accept connection: \(sender.domain)")
        self.outputStream = outputStream
        inputStream.delegate = self
        outputStream.delegate = self

        outputStream.schedule(in: .current, forMode: .commonModes)
        inputStream.schedule(in: .current, forMode: .commonModes)
        
        inputStream.open()
        outputStream.open()
    }
    
}

extension ReceivedEvent: RawRepresentable {
    typealias RawValue = [String: Any]

    init(rawValue: RawValue) {
        guard let value = rawValue["type"] as? String, let type = ReceivedEventType(rawValue: value) else {
            self = .invalid
            return
        }
        
        switch type {
        case .send_file:
            self = .fileSendRequest(
                hostName: rawValue["devicename"]! as! String,
                name: rawValue["filename"]! as! String,
                fileLength: rawValue["fileLength"]! as! Int64
            )
        }
    }
    
    var rawValue: [String : Any] {
        return ["foo" : "bar"]
    }
}

fileprivate protocol SentEvent {
    func serialize() -> [String: String]
}

fileprivate class HandshakeEvent: SentEvent {
    func serialize() -> [String: String] {
        return ["type" : "handshake"]
    }
}

fileprivate class AcceptEvent: SentEvent {
    func serialize() -> [String: String] {
        return ["type" : "send_file_response", "response" : "accepted"]
    }
}

fileprivate class DenyEvent: SentEvent {
    func serialize() -> [String: String] {
        return ["type" : "send_file_response", "response" : "denied"]
    }
}

struct FileProposition {
    let hostName: String
    let files: [File]
}

struct File {
    let filename: String
    let fileLength: Int64
}
