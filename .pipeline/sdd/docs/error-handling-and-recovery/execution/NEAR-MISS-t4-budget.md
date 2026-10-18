# Near-miss: the livelock fix shipped with the livelock in it

Found during the T-4 finish pass, wave 2. Worth recording in full because of how
close it came to landing and why every automated gate would have passed it.

## What the code said

`AdbHostSourceImpl.startServer()` carried KDoc stating, two lines above the call:

> `DaemonStartTimeoutMs`, not `HostCommandTimeoutMs`

and then passed `MirrorTuning.HostCommandTimeoutMs`.

`HostCommandTimeoutMs` is 3_000 ms. A cold `adb` daemon fork is a measured, fixed
3.03 s. So `startServer()` — **the function whose entire reason for existing is to
survive the cold fork** — was given the exact budget that cannot survive it.

The fix for the livelock would have shipped with the livelock inside it.

## Why nothing caught it

- **It compiles.** Both constants are `Long`, so the wrong one is type-correct.
- **The previous agent ran a compile check and died before running tests.** Its
  compile-only verification was green and would have been reported as progress.
- **The test that catches it already existed, written by that same agent.** It was
  never executed. A written test is not a passing test, and a passing test is not a
  proven test.
- **Code review would plausibly miss it.** The KDoc immediately above says the right
  thing. A reader checking "does the comment describe the intent?" sees agreement and
  moves on; only a reader diffing the comment against the argument sees it.

This is the lying-comment defect class: the comment and the code disagreed, and the
comment was the more convincing of the two.

## How it was found

The finishing agent ran the tests the dead one had written. The budget test failed
immediately. One line changed:

`data-device-impl/src/desktopMain/.../source/datasource/AdbHostSourceImpl.kt:46`
→ `timeoutMs = MirrorTuning.DaemonStartTimeoutMs`

`killServer` (line 55) correctly kept `HostCommandTimeoutMs` — a local socket close,
not a fork. `listDevices` (line 30) untouched.

## The mutation under-killed, and three were needed

The prescribed mutation moves only one half of the assertion. The agent flagged this
rather than accepting a half-proof, and ran three independent mutations from the
reverted-good file:

| mutation | what it proves |
|---|---|
| `startServer` → `HostCommandTimeoutMs` (the shipped defect) | the watchdog half is load-bearing |
| `killServer` → `DaemonStartTimeoutMs` | the 3 s half is load-bearing too |
| `startServer` builds `HostCommand.KillServer` | the argv assertion is load-bearing |

Three distinct reds, then a `diff`-clean revert and green. Note the budget test takes
6.5 s because it genuinely pays both the 3.2 s fork and the 3 s timeout — a real
clock, not a stub. Slow, and worth it: a stubbed clock would not have caught a wrong
constant, only a wrong comparison.

## Standing lessons

1. **A written test is not a run test.** Any task interrupted before its test run must
   be treated as unverified regardless of how complete the code looks, and a
   compile-only check must never be reported as verification.
2. **When a comment and the code disagree, trust neither** — run the test that
   distinguishes them. Here the comment was right and the code was wrong; the previous
   instance of this class in this chain (F-4's vacuous session-loss test) was the
   reverse.
3. **A mutation that moves one assertion of several is a half-proof.** Flag it and run
   one mutation per assertion, as this agent did.

---

# Second instance of the same class — this one was mine

While repairing the tree the dead T-20 agent broke, I added
`DeviceRepository.setAdbPath`, its `DeviceRepositoryImpl` override, the constructor
parameter and the extra `get()` in the Koin module. I then ran:

```shell
./gradlew :data-repository-impl:compileKotlinDesktop :domain-usecase-impl:compileKotlinDesktop
```

Both green, and I reported "the whole tree compiles again".

**It did not.** `compileKotlinDesktop` builds the *main* source set. Adding a
constructor parameter breaks every construction site, and two of them live in
`data-repository-impl`'s own **test** source set:

```
DeviceDiscoveryPollingTest.kt:236:26  No value passed for parameter 'adbPathPreferenceSource'.
DeviceToolDetectionTest.kt:71:9       No value passed for parameter 'adbPathPreferenceSource'.
```

The T-21 agent hit this immediately and had to touch the two files I had told it not
to, plus add a `FakeAdbPathPreferenceSource`. It flagged the overreach rather than
quietly working around it, which is the only reason the cause is on record.

## Why this keeps happening

This is the **third** instance in this session of verifying something narrower than
the claim made about it:

| | claim | what was actually checked |
|---|---|---|
| T-4's dead agent | "implemented" | compiled, never ran the test it had written |
| my baseline | "195 tests pass" | counted stale XML in `build/`, not a fresh run |
| my repair | "the tree compiles" | main source sets only, not test source sets |

The pattern is identical each time: a green command gets reported as a broader
guarantee than it gives.

## Standing rule

A constructor-signature change is verified only by a command that compiles the
**test** source sets too, for every target:

```shell
./gradlew :<module>:compileTestKotlinDesktop :<module>:compileTestKotlinIosArm64
```

and the honest full check before any wave is called done is the real suite plus the
cross-target test compile — never `compileKotlinDesktop` alone, and never a count
scraped from whatever XML happens to be lying in `build/`.
