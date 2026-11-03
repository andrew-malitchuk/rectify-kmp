import AppKit
import Foundation

// args: <in.svg> <out.png> <px>
let a = CommandLine.arguments
guard a.count == 4, let px = Int(a[3]) else { exit(2) }
guard let src = NSImage(contentsOfFile: a[1]) else {
    FileHandle.standardError.write("NSImage could not read the SVG\n".data(using: .utf8)!); exit(3)
}
guard let rep = NSBitmapImageRep(bitmapDataPlanes: nil, pixelsWide: px, pixelsHigh: px,
        bitsPerSample: 8, samplesPerPixel: 4, hasAlpha: true, isPlanar: false,
        colorSpaceName: .deviceRGB, bytesPerRow: 0, bitsPerPixel: 0) else { exit(4) }
rep.size = NSSize(width: px, height: px)
NSGraphicsContext.saveGraphicsState()
NSGraphicsContext.current = NSGraphicsContext(bitmapImageRep: rep)
NSGraphicsContext.current?.imageInterpolation = .high
// Transparent ground, then the art scaled to fill — no white composite.
NSColor.clear.set()
NSRect(x: 0, y: 0, width: px, height: px).fill()
src.draw(in: NSRect(x: 0, y: 0, width: px, height: px),
         from: .zero, operation: .sourceOver, fraction: 1.0)
NSGraphicsContext.restoreGraphicsState()
guard let data = rep.representation(using: .png, properties: [:]) else { exit(5) }
try data.write(to: URL(fileURLWithPath: a[2]))
