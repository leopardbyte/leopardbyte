import UIKit
import CoreData
import Lottie

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    // ... other app delegate methods ...
}

// ViewController to track work times
class TimeTrackerViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
    // UI elements
    var startButton: UIButton!
    var stopButton: UIButton!
    var timerLabel: UILabel!
    var tableView: UITableView!
    var startAnimationView: AnimationView!
    var stopAnimationView: AnimationView!
    
    // Data model
    var workSessions: [WorkSession] = []
    
    // Date formatter
    let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter
    }()
    
    // Setup UI elements and Lottie animations
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupAnimations()
        fetchWorkSessions()
    }
    
    // Initialize and configure start/stop buttons, timer label, and table view
    func setupUI() {
        // ... UI setup code ...
        
        // Initialize table view
        tableView = UITableView()
        tableView.delegate = self
        tableView.dataSource = self
        view.addSubview(tableView)
        
        // Register cell class
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "cell")
    }
    
    // Initialize Lottie animations
    func setupAnimations() {
        // ... Lottie setup code ...
    }
    
    // Handle start button tap
    @objc func startButtonTapped() {
        // Start tracking work time
        // Play start animation
        startAnimationView.play()
        
        // Create a new WorkSession object
        let newSession = WorkSession(context: persistentContainer.viewContext)
        newSession.startTime = Date()
        workSessions.append(newSession)
    }
    
    // Handle stop button tap
    @objc func stopButtonTapped() {
        // Stop tracking work time
        // Play stop animation
        stopAnimationView.play()
        
        // Save the session to CoreData
        if let currentSession = workSessions.last {
            currentSession.endTime = Date()
            currentSession.duration = currentSession.endTime?.timeIntervalSince(currentSession.startTime ?? Date()) ?? 0
            saveWorkSession()
        }
    }
    
    // Save work session to CoreData
    func saveWorkSession() {
        do {
            try persistentContainer.viewContext.save()
        } catch {
            // Handle the error
        }
    }
    
    // Fetch work sessions from CoreData
    func fetchWorkSessions() {
        let fetchRequest: NSFetchRequest<WorkSession> = WorkSession.fetchRequest()
        do {
            workSessions = try persistentContainer.viewContext.fetch(fetchRequest)
        } catch {
            // Handle the error
        }
    }
    
    // TableView DataSource methods
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return workSessions.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath)
        let session = workSessions[indexPath.row]
        cell.textLabel?.text = "Worked from \(dateFormatter.string(from: session.startTime ?? Date())) to \(dateFormatter.string(from: session.endTime ?? Date()))"
        return cell
    }
    
    // ... other methods to handle UI and data ...
}

// CoreData Entity for work sessions
class WorkSession: NSManagedObject {
    @NSManaged var startTime: Date?
    @NSManaged var endTime: Date?
    @NSManaged var duration: TimeInterval
    // ... other properties and methods ...
}

// ... additional classes and extensions for animations, CoreData, etc. ...
