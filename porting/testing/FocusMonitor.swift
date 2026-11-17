import AppKit
import Foundation

// Read-only observation of the foreground application; never activates an app.
let seconds = Double(CommandLine.arguments.dropFirst().first ?? "15") ?? 15
func foreground() -> String {
    guard let app = NSWorkspace.shared.frontmostApplication else { return "none" }
    return "\(app.processIdentifier):\(app.bundleIdentifier ?? app.localizedName ?? "unknown")"
}
let original = foreground()
print("Initial foreground: \(original)")
fflush(stdout)
let end = Date().addingTimeInterval(seconds)
var samples = 0
var changes: [String] = []
while Date() < end {
    let current = foreground()
    if current != original && changes.last != current {
        changes.append(current)
    }
    samples += 1
    Thread.sleep(forTimeInterval: 0.02)
}
let report: [String: Any] = ["initial": original, "samples": samples, "changes": changes]
let data = try JSONSerialization.data(withJSONObject: report, options: [.sortedKeys])
print(String(decoding: data, as: UTF8.self))
