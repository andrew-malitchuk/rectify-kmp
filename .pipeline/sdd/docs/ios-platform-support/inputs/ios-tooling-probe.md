# iOS tooling probe — measured on this machine, 2026-10-19

Run before Stage 1 so the plan is built on what is actually installed rather than assumed.

| tool | result |
| --- | --- |
| `xcrun simctl` | **present**, Xcode **26.3** (build 17C529). `simctl list devices` reports runtimes **iOS 17.5** and **iOS 18.1**. |
| `idb` | **present** at `/Users/andrew-malitchuk/.local/bin/idb` |
| `idb_companion` | **present** at `/opt/homebrew/bin/idb_companion` |
| booted simulators | **none** at probe time |

## Why this matters to the plan
- Unlike F-9's GUI demo (waived as unexecutable here), **the iOS adapter layer can genuinely be
  exercised**: `simctl list -j` and `idb list-targets` are real commands this machine can run.
- **No simulator is booted**, which is the natural reproduction of AC-31's
  *simulator-not-booted* state — it costs nothing to test and should not be faked.
- The *device-not-trusted* half of AC-31 needs a physical, untrusted iPhone. Note a physical
  **Android** device is attached (`38031FDJH00JVF`, seen during F-9), but that proves nothing
  for iOS. Expect this half to be fake-driven and say so rather than implying coverage.
- `NFR-10` says v1 targets **macOS only** — which this host is, so nothing is blocked there.

## Do not assume
Whether `idb_companion` is actually *running* (it is a daemon), whether any simulator can be
booted in this environment, and whether `idb` is authorised against a physical device are all
separate questions from "the binary exists". Probe them when the task needs them.
