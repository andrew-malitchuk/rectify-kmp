# Installation

Rectify is not published as a binary yet. You build it from source, which takes one Gradle
command, and you wire its MCP server into Claude Code, which takes one JSON entry.

## Requirements

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

## Run it

```shell
git clone https://github.com/andrew-malitchuk/rectify-kmp.git
cd rectify-kmp
./gradlew :desktop-application:run
```

The window opens at 1240×820 and starts polling for devices immediately.

## Wire up the MCP server

Rectify writes each sent batch to a handoff slot on disk. A separate, very small server process
reads that slot back out and answers Claude Code over stdio. Claude Code starts and stops that
process itself — you never run it by hand.

**Build the launcher first.** There is no packaged distribution, so the binary has to exist
before Claude Code can spawn it:

```shell
./gradlew :mcp-application:installDist
```

That produces `mcp-application/build/install/rectify-mcp/bin/rectify-mcp`.

**Then register it.** Copy the example config at the repository root:

```shell
cp .mcp.json.example .mcp.json
```

```json
{
  "mcpServers": {
    "rectify": {
      "command": "./mcp-application/build/install/rectify-mcp/bin/rectify-mcp",
      "args": []
    }
  }
}
```

The path is relative to wherever Claude Code is started. If you run Claude Code from a different
directory than the Rectify checkout — which is the normal case, since you are working on *your*
project, not on Rectify — make it absolute.

Restart Claude Code and it will list a `rectify` server exposing one tool,
`get_latest_annotation`.

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
