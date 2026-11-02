# Installation

## Quick install

The fastest path — no build tools required.

1. Download `Rectify-*.dmg` from the
   [latest GitHub Release](https://github.com/andrew-malitchuk/rectify-kmp/releases/latest).
2. Open the DMG and drag **Rectify** into `/Applications`.
3. The build is unsigned and unnotarised, so Gatekeeper refuses it on first launch.
   Right-click the app and choose **Open**, or clear the quarantine flag:

   ```shell
   xattr -dr com.apple.quarantine /Applications/Rectify.app
   ```

4. Open Rectify and [point Claude Code at it](#wire-up-the-mcp-server) — one command, which
   the app itself puts on your clipboard.

> **Apple Silicon only.** Compose Desktop packages for the OS and architecture it is built
> on, and the release is built on arm64. On an Intel Mac, build from source.

> **The About box says 1.0.0.** jpackage refuses a macOS bundle version whose first component
> is zero, so the bundle carries `1.0.0` while the release is `0.1.0`. The tag and the
> changelog are the source of truth.

---

## Build from source

### Requirements

| | |
|---|---|
| **Host** | macOS. The device tooling below is macOS-first, and packaging targets the OS you build on. |
| **JDK** | 21 or newer. |
| **Android devices** | `adb` on your `PATH`, or an Android SDK at `$ANDROID_HOME` / `$ANDROID_SDK_ROOT`. |
| **iOS simulators** | Xcode command line tools — `xcrun` and `simctl`. `idb` as well if you want physical devices. |

You need neither toolchain to *launch* Rectify. It detects what is present and reports the rest
as a recoverable error card rather than refusing to start.

> **A note on packaging.** `./gradlew packageDmg` currently fails on a Homebrew-installed JDK —
> the Compose plugin blocks it because such builds produce broken bundles. Use a distribution
> such as Amazon Corretto if you want an installer. Running from Gradle is unaffected.

### Run it

```shell
git clone https://github.com/andrew-malitchuk/rectify-kmp.git
cd rectify-kmp
./gradlew :desktop-application:run
```

The window opens at 1240×820 and starts polling for devices immediately.

## Wire up the MCP server

Rectify serves the Model Context Protocol itself, on the loopback interface, for as long as the
app is open. There is nothing to install and nothing to spawn — no second binary, no JDK of its
own, no absolute path to get wrong.

Click **`MCP :7391`** in Rectify's status bar. That copies the registration line for the port
this instance actually bound:

```shell
claude mcp add --transport http rectify http://127.0.0.1:7391/mcp
```

Or write it into a config file yourself — copy the example at the repository root:

```shell
cp .mcp.json.example .mcp.json
```

```json
{
  "mcpServers": {
    "rectify": {
      "type": "http",
      "url": "http://127.0.0.1:7391/mcp"
    }
  }
}
```

Restart Claude Code and it will list a `rectify` server exposing one tool,
`get_latest_annotation`.

### What the status bar tells you

| Reads | Means |
|---|---|
| `MCP :7391` | The endpoint is up on the default port. Click to copy the registration line. |
| `MCP :7392` (or higher) | 7391 was taken, so Rectify moved. Re-register with the copied line. |
| `MCP off` | No port in the scan range could be bound. Everything except the handoff still works. |

### Two things the endpoint deliberately refuses

- **Requests from anywhere but this machine.** It binds the loopback address only, never the
  wildcard — an MCP endpoint on `0.0.0.0` would offer your latest screenshots to everyone on the
  network.
- **Cross-origin requests.** Any page in your browser can POST to `127.0.0.1`. Browsers always
  send an `Origin` header on such a request, and a non-local one is refused, so a website you
  happen to have open cannot read what you were debugging.

**The app has to be running.** This is the trade against the old out-of-process server, which
could answer while Rectify was closed. In practice the app is open anyway — you just annotated
in it, and you will return to it for Verify.

## Verify the wiring

With Rectify running and at least one batch sent, ask Claude Code to call the tool. It should
return the most recent annotation batch. If it returns nothing, the two halves are looking at
different places — see [Troubleshooting](../troubleshooting/).

## Where Rectify keeps its data

Everything lives under `~/.rectify/`, created on first use, owner-only where the filesystem
supports POSIX permissions:

| Path | Contents |
|---|---|
| `~/.rectify/handoff` | The latest published batch — what the MCP server reads. |
| `~/.rectify/history/` | Per-batch frames and crops backing the History tab. |
| `~/.rectify/rectify.db` | SQLite database of sent batches. |

Preferences (theme, language, panel width, chosen `adb` path) are stored separately through the
JVM's own `java.util.prefs` under the node `dev.rectify.io`.
