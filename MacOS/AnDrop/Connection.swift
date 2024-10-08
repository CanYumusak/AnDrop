
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
            
            let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
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
        guard let files = self.files else {
            print("started file transfer without file array")
            return
        }
        currentlyTransferringFile = true
        
        let fileTransferResults : [FileTransferState] = files.map { file in
            
            let fileLength = file.fileLength
            let filename = file.filename
            let creationDate = file.creationDate
            
            var data = Data()
            let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: self.maxLength)
            while data.count < fileLength && stream.streamStatus == .open {
                let maxLength = min(self.maxLength, Int64(data.count).distance(to: fileLength))
                let length = stream.read(buffer, maxLength: maxLength)
                data.append(buffer, count: length)
            }
            
            if (data.count >= fileLength) {
                print("file transfer finished")
                if let dir = downloadFolder {
                    do {
                        let fileURL = dir.appendingPathComponent(filename)
                        try data.write(to: fileURL)
                        
                        if let creationDate {
                            let nsCreationDate = Date(timeIntervalSince1970: TimeInterval(creationDate/1000))
                            let attributes = [FileAttributeKey.creationDate: nsCreationDate, FileAttributeKey.modificationDate: nsCreationDate]
                            
                            
                            do {
                                try FileManager.default.setAttributes(attributes, ofItemAtPath: fileURL.path)
                                print("set creation date to \(nsCreationDate) for file \(fileURL.path)")
                                
                                let readCreationDate = try FileManager.default.attributesOfItem(atPath: fileURL.path)[FileAttributeKey.creationDate]
                                print("Read Creation Date: \(readCreationDate)")
                            
                            }
                            catch {
                                print("error while writing creation attribute \(error)")
                            }
                        }
                        
                        return .success(url: fileURL)
                    } catch {
                        print("error while writing file \(error)")
                        return .failed(filename: filename)
                    }
                } else {
                    return .failed(filename: filename)
                }
                
            } else {
                print("file transfer cancelled")
                return .failed(filename: filename)
            }
        }
        
        waitingForFile = false
        currentlyTransferringFile = false
        filePropositionDelegate.showFileTransferFinishedNotification(states: fileTransferResults)
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

        outputStream.schedule(in: .current, forMode: RunLoop.Mode.common)
        inputStream.schedule(in: .current, forMode: RunLoop.Mode.common)
        
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
            let rawFiles = rawValue["files"] as! [[String: Any]]
            let files = rawFiles.map { rawFile in
                let filename = rawFile["fileName"] as! String
                let fileLength = rawFile["fileLength"]  as! Int64
                let creationDate = rawFile["creationDate"] as? Int64

                return File(filename: filename, fileLength: fileLength, creationDate: creationDate)
            }
            
            self = .fileSendRequest(
                hostName: rawValue["deviceName"]! as! String,
                files: files
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
    let creationDate: Int64?
}
