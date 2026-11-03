# Troubleshooting

Rectify reports problems as structured error cards on the stage rather than as dialogs or log
lines: an icon, a title, a plain-language body, a copyable command where one would help, a
primary and secondary action, and a diagnostic footer showing exactly what was observed.

The footer is the part worth reading when the card itself does not match your situation.

## adb is not on the PATH

Rectify probes, in order: the path you chose yourself, `$ANDROID_HOME`, `$ANDROID_SDK_ROOT`,
`~/Library/Android/sdk`, `/opt/homebrew/bin`, `/usr/local/bin`, then `PATH`.

`PATH` alone is not enough, because a GUI application launched from Finder does not inherit the
shell `PATH` your terminal has — which is why the explicit candidate list exists.

**Fix it** by installing the platform tools, or use **Locate adb…** on the card to point at the
binary directly. Your choice is remembered and re-read on every probe, so changing it mid-session
takes effect without a restart.

## No device detected

`adb` answers but sees nothing attached.

- USB debugging enabled on the handset?
- **Allow** tapped on the authorisation prompt? An unauthorised device is attached but unusable,
  and the card counts it separately.
- Cable capable of data, not just charge?

**Restart adb server** on the card runs `adb kill-server` and lets the next poll start it again.
This is the single most effective action for a device that was working a minute ago.

## Multiple devices connected

Not an error — an ambiguity. The card lists what it found inline and defaults its primary action
to the top one. Pick the one you meant, or use the toolbar chip to open the full picker.

## No simulator is running

Xcode knows about your simulators but none is booted. The card offers **Boot simulator** and a
copyable `xcrun simctl boot <udid>` line.

Note the command addresses the simulator by udid, never by name: two simulators can legitimately
share a name under one runtime, so the name does not identify what the button would boot.

## iPhone not trusted

Unlock the device and tap **Trust** on the prompt, then retry. The card also offers **Use
simulator** as a way out, since a simulator needs no trust relationship.

## Xcode command line tools missing

Install Xcode, then run `xcode-select --install`. Rectify uses `xcrun` and `simctl` for
simulators, and `idb` for physical devices.

## The stream dropped mid-session

A floating banner over the stale frame, not an error card — your annotations are kept, and
**Resolve & Send** still works. Press **Reconnect** when the device is back.

## Claude Code gets nothing from the tool

Work through these in order:

1. **Did a batch actually send?** Check the History tab. No row means no payload.
2. **Was the launcher built?** `./gradlew :mcp-application:installDist` must have run at least
   once. There is no packaged distribution.
3. **Is the path right?** The command in `.mcp.json` is relative to wherever Claude Code was
   started, not to the Rectify checkout. Make it absolute if the two differ.
4. **Same user account?** The slot is at `~/.rectify/handoff` and is owner-only. Rectify and
   Claude Code must run as the same user.

The server writes diagnostics to stderr; Claude Code's MCP logs will show them.

## The status bar as a diagnostic

The bottom line is the honest account of what the machine is doing:

```
● LIVE   adb Android Debug Bridge version 1.0.41 · 1080x2400 · 0.3 fps · polling · latency —
```

A tool version proves the binary was found and answered. A resolution proves a frame decoded.
`0.0 fps` with a device selected means the capture loop is not producing frames, which is a
different problem from not finding the device at all.
