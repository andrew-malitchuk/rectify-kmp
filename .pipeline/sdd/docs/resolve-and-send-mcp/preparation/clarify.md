# resolve-and-send-mcp — clarify

## Envelope

| field | value |
| --- | --- |
| stage | preparation |
| step | clarify |
| status | done |
| producedBy | sdd-build-requirements-clarify |
| schemaVersion | 1 |
| createdAt | 2026-10-18T10:36:32Z |
| updatedAt | 2026-10-18T10:36:32Z |

## Gate

### type

human

### approvedBy

human

### approved

true

### at

2026-10-18T10:36:32Z

## Provenance

### selfHash

67166dff5cc1f4ff37a40bfcd535d7d6d1eb37603be5d861e082e8c40eed2f8b

### inputs

| artifact | hash |
| --- | --- |
| preparation/input.json | 984bb3fdd6caffdcad5fd46524735e64febb9d77c2e07fbda3a73ac7c6a8561e |

## Data

### overview

F-7 'Resolve & Send via Local MCP' turns the toolbar's staged-annotation batch into an agent-actionable handoff: a counter + Clear + 'Resolve & Send' button (disabled at zero staged / while sending), an animated Streaming->Frozen->Dumping hierarchy->Sent status sequence, a completion toast with an 'Open session' action, and a resumed live mirror with the counter reset to 0. The PRD text names 'the existing local rectify MCP server', but a repo-wide grep of every *.kt/*.kts/*.swift/*.toml file found no MCP server, MCP client, JSON-RPC plumbing or get_latest_annotation anywhere in source — every hit lives in .pipeline/ prose. This clarification therefore resolves the transport/server question as a closed scoping decision (see proposedSurvey Q1) before any plan work can start, and settles six of the eight ambiguities directly from code precedent (F-3's Reconnect semantics, F-5/F-6's Android-only hierarchy boundary, F-6's un-serialized PAYLOAD_FORMAT_VERSION const, and the version catalog's ktor-client-only/no-ktor-server/no-MCP-SDK footprint) without spending a survey question on them.

### problemStatement

Nothing in this repo currently transmits a payload anywhere. F-6 (merged) assembles the full bundle (frame screenshot, crops, hierarchy XML, generated prompt, an internal PAYLOAD_FORMAT_VERSION=1 const) but explicitly stops short of sending it — 'nothing writes frame.png / hierarchy.xml yet; the send spec owns replacing both heuristics with measured values.' F-7 is that send spec, and its PRD wording presumes transport infrastructure (a 'rectify' MCP server) that does not exist in this codebase, in any sibling spec, or as a dependency in gradle/libs.versions.toml (ktor-client-* is declared but unused anywhere; there is no ktor-server-* entry and no MCP/JSON-RPC SDK at all).

### purpose

Define, as closed options the orchestrator can run past the user, exactly what F-7 builds: where the MCP server lives (this repo vs. external vs. a bounded in-repo stub), the retrieval tool's name, the transport mechanism, and the 'Open session' toast action's semantics — then mint/confirm the requirement set (reusing FR-16..19, NFR-3/4/12, AC-17..21/32 unchanged) so plan.py has an unambiguous, buildable scope instead of a PRD sentence that contradicts the codebase.

### goals

- Resolve the single largest scoping fork (does F-7 build an MCP server, a client-only publisher, or a bounded stub) before plan.py starts, with evidence-backed reasoning recorded in the survey rationale.
- Settle every ambiguity answerable purely from existing code/precedent (hierarchy Android-only, PAYLOAD_FORMAT_VERSION not yet serialized, failure recovery following F-3's Reconnect pattern, async send) as assumptions rather than survey questions.
- Reuse FR-16..19 / NFR-3,4,12 / AC-17..21,32 verbatim by id; mint new ids (FR-56..58, NFR-15, AS-28..31, SC-25) only for genuinely new scope this clarify pass surfaces.
- Keep the confirmed scope inside this repo's actual constraints: Android+iOS+Desktop-JVM only, convention-plugin-shaped modules, no new third-party dependency beyond what's already cataloged (kotlinx-serialization) unless the survey answer requires one.

### acceptanceCriteria

| id | text | priority | verificationMethod | confirmed |
| --- | --- | --- | --- | --- |
| AC-17 | Given zero annotations are staged, then 'Resolve & Send' is disabled; given one or more are staged, it is enabled; 'Clear' removes all staged annotations and resets the counter to 0. | must | test | true |
| AC-18 | When the user clicks 'Resolve & Send', then the frame screenshot, per-annotation crops, hierarchy XML, and generated prompt are transmitted via the local MCP server (per survey Q1/Q3: an in-repo stdio MCP server stub spawned/hosted by the desktop app) such that a Claude Code session calling the retrieval tool (per survey Q2) receives the complete payload. | must | test | true |
| AC-19 | When sending, then the status sequence Streaming -> Frozen -> Dumping hierarchy -> Sent displays in order with colored dots, and on completion a toast shows the sent-count/time summary with an 'Open session' link. | must | demo | true |
| AC-20 | While a send is in progress, 'Resolve & Send' is disabled and only re-enables after the toast confirms or the send fails; on failure, staged annotations and the frozen frame are NOT cleared (AS-31), so the user can retry with the same batch. | must | test | true |
| AC-21 | After a successful send, then live mirroring resumes automatically and the staged-annotation counter resets to 0. | must | test | true |
| AC-32 | Given a staged annotation batch is sent, then the persisted payload envelope carries an explicit format/schema version field (serialized from F-6's existing but currently unserialized PAYLOAD_FORMAT_VERSION=1 const, per AS-28) that the receiving MCP server can read to select its parsing path. | should | inspection | true |
| AC-81 | Given the in-repo MCP server stub is running and a payload has been sent, when a client calls the retrieval tool a second time with no new send in between, then it returns the same last-sent payload (idempotent 'latest' semantics) rather than an error or an empty result. | must | test | true |

### functionalRequirements

| id | text |
| --- | --- |
| FR-16 | In the toolbar, show a staged-annotation counter, a 'Clear' action that removes all staged annotations, and a primary 'Resolve & Send' button that is disabled while zero annotations are staged or while a send is in progress. |
| FR-17 | On 'Resolve & Send', package the full frame screenshot, per-annotation crops, view-hierarchy XML dump, and generated prompt into a versioned envelope, and publish it so a locally running Claude Code agent can retrieve it by calling a single MCP tool (name/transport per survey Q2/Q3) against an in-repo stdio MCP server stub that this spec builds and hosts from the desktop app (survey Q1) — not a pre-existing external server, which this codebase does not contain. |
| FR-18 | During send, show a visible status sequence (Streaming -> Frozen -> Dumping hierarchy -> Sent) with colored dots, running asynchronously off the UI thread (AS-30), then confirm with a toast ('Sent N annotations to Claude Code · frame + hierarchy · <time>s') including an 'Open session' link. |
| FR-19 | After a successful send, resume live mirroring automatically and reset the staged-annotation counter to 0. |
| FR-56 | Rectify desktop hosts/spawns a local, stdio-transport MCP server process exposing exactly one retrieval tool (name per survey Q2) that returns the most recently sent payload envelope; this is new infrastructure this spec builds, not integration with a pre-existing server. |
| FR-57 | On 'Resolve & Send', serialize the payload envelope — frame screenshot, per-annotation crops, hierarchy XML, generated prompt, and the PAYLOAD_FORMAT_VERSION field (AS-28) — and publish it to the in-process store the MCP server's retrieval tool reads from. |
| FR-58 | The toast's 'Open session' action copies the current session identifier to the system clipboard (survey Q4 default), giving the user something to paste into their own Claude Code terminal/session picker without this repo needing to focus or launch an external application window. |

### nonFunctionalRequirements

| id | category | text |
| --- | --- | --- |
| NFR-3 | performance | Resolve & Send payload transmission to the local MCP server should typically complete in under ~2s on localhost for a typical batch. |
| NFR-4 | security | All mirror stream data, screenshots, hierarchy dumps, and payloads stay local to the machine; no network egress beyond localhost MCP communication — trivially satisfied by a stdio transport, which never opens a network socket at all. |
| NFR-12 | maintainability | The payload format (prompt + crops + hierarchy XML) is versioned so the receiving MCP server/Claude Code agent can evolve its parsing without breaking older sends. |
| NFR-15 | maintainability | The MCP server component ships as a convention-plugin-shaped module (library or application plugin, per build-logic/convention) hosted/spawned by :desktop-application's main(), introducing no bespoke Gradle and no new third-party dependency beyond the already-cataloged kotlinx-serialization unless the survey's transport answer requires one. |

### outOfScope

- File-drop or webhook fallback transport — MCP (stdio, per Q3 recommendation) is the sole transport in v1, per PRD.
- Full production-grade / spec-compliant MCP server hardening: multi-client support, auth/pairing handshake, tool discovery/listing beyond the single retrieval tool, reconnection of the MCP transport itself. Survey Q1's recommended option explicitly defers this to a follow-up once AC-18/AC-81 are proven end to end.
- Any work belonging to F-8 (verify-and-rediff-loop) or F-9 (history-batch-tracking).
- iOS hierarchy dump / iOS payload send — inherits F-5/F-6's Android-only boundary (AS-19, AS-23, AS-29).
- Replacing or upgrading Claude Code's own MCP client config mechanism — this spec only ships the server side the user's existing Claude Code MCP config would point at (tracked as an openQuestion, not blocking).

### constraints

| type | text |
| --- | --- |
| tech | Android, iOS, and Desktop-JVM only (platform-coverage.md) — there is no macOS/Linux native target; the MCP server is Desktop-JVM-hosted only, same as :desktop-application's existing scope. |
| tech | gradle/libs.versions.toml has ktor-client-* (core/okhttp/darwin/cio/websockets/content-negotiation) declared but used in zero build.gradle.kts files, and no ktor-server-* or MCP/JSON-RPC SDK entry exists at all. A stdio transport needs no new dependency (kotlinx-serialization + System.in/out suffice); an HTTP/TCP transport would need a new ktor-server-* catalog entry. This is direct evidence for the Q3 recommendation. |
| tech | New module(s) must go through build-logic/convention's existing plugins (application/feature/library/di) per CLAUDE.md — no bespoke Gradle for the MCP server module. |
| tech | package-layout.md and explicitApi(): the MCP server's wire format / tool contract types are public API (source/), its stdio framing/process plumbing is internal (core/). |
| tech | testing.md: commonTest names must avoid commas and colons; desktopTest is the verification runner; no Compose UI-test harness exists. |

### assumptions

| id | text |
| --- | --- |
| AS-28 | F-6's PAYLOAD_FORMAT_VERSION = 1 is today an internal const declared in core/payload and 'rendered nowhere' (per F-6's own plan: 'nothing writes frame.png / hierarchy.xml yet') — it is NOT currently serialized into any transmitted bytes, because nothing is transmitted yet. F-7 is the spec that reads this const and embeds it into the actual envelope for AC-32. Verified by reading F-6's plan.data.json directly; not a product question. |
| AS-29 | The hierarchy-dump phase (FR-18's 'Dumping hierarchy' step) stays Android-only, matching measure-mode's AS-19 and payload-preview's AS-23 precedent — both already-shipped specs drew this exact line and F-7 sends whatever F-6 already assembled (empty/absent hierarchy field when HierarchyStatus is Absent/Unavailable, unconditionally for iOS). |
| AS-30 | Send runs asynchronously, not blocking the UI: FR-18's four-step animated status sequence and AC-20's 'disabled while in flight, re-enables after toast or failure' only make sense if the UI keeps rendering and reacting to status updates while the send is in progress. |
| AS-31 | On a failed send, staged annotations and the frozen frame are NOT cleared — following F-3's shipped Reconnect precedent (FR-33/FR-35: involuntary disconnect preserves frame+annotations, a distinct code path from the user-initiated Resume Live clear-and-return). 'Resolve & Send' simply re-enables (AC-20) so the user can retry the same batch; only explicit Clear or Resume Live discards staged annotations. |
| AS-32 | The MCP server this spec builds is a bounded stub sufficient to prove AC-18/AC-81 (a working stdio JSON-RPC process exposing one retrieval tool, returning the last-sent envelope) — not a fully spec-compliant, multi-session-capable MCP implementation. Hardening is explicit future work, not silently assumed-away scope. |

### dependencies

| on | note |
| --- | --- |
| payload-preview (F-6, merged) | Supplies the assembled bundle (frame screenshot, crops, hierarchy XML, generated prompt) and the internal PAYLOAD_FORMAT_VERSION=1 const that F-7 serializes into the transmitted envelope (AS-28). |
| error-handling-and-recovery (F-3, merged) | Its Reconnect/stream-drop precedent (FR-33/FR-35: preserve frame+annotations on involuntary failure, distinct from user-initiated clear) is the basis for AS-31's send-failure recovery behavior. |
| measure-mode (F-5, merged) / payload-preview (F-6, merged) | Both establish the Android-only hierarchy-dump boundary (AS-19, AS-23) that F-7 inherits unchanged (AS-29). |
| Claude Code's own MCP client configuration | Out of this repo's control: the user must point their Claude Code MCP config at whatever this spec's stdio server process is (exact discovery/config mechanism is an openQuestion, not blocking plan.py). |

### scenarios

| id | title | type | steps |
| --- | --- | --- | --- |
| SC-7 | Resolve & Send happy path | happy | ["Click Resolve & Send", "Status sequence animates through each stage", "Toast confirms with Open session link", "Mirroring resumes and counter resets"] |
| SC-25 | Send fails mid-flight and the user retries | error | ["Stage one or more annotations and click Resolve & Send", "The MCP transport fails mid-send (e.g. no client currently listening)", "Status sequence shows the failure variant; staged annotations and the frozen frame remain exactly as before (AS-31)", "'Resolve & Send' re-enables (AC-20)", "User clicks Resolve & Send again and the retry succeeds with the same batch"] |

### successMetrics

- A Claude Code session calling the agreed retrieval tool against the in-repo stdio MCP server receives the complete, version-tagged payload envelope for the most recent send (AC-18, AC-81).
- Typical localhost sends complete in under ~2s (NFR-3), with zero network egress (NFR-4) since the transport is stdio only.
- A failed send never loses staged annotations or the frozen frame (AS-31, AC-20).

### openQuestions

- Exact mechanism by which a user's Claude Code MCP config discovers/launches this repo's stdio server process (a generated .mcp.json entry? a fixed script path Rectify writes on first run?) is undefined — needs an engineering decision in the plan stage, not blocking clarify.
- Whether the bounded MCP server stub (AS-32) should live in its own new module or inside presentation-feature-mirror/compose-application is an architecture decision deferred to plan.py.
- Long-term hardening path for the MCP server (multi-client, auth/pairing, full protocol compliance) is explicitly out of v1 scope but should be tracked as a follow-up feature once F-7 ships.

### proposedSurvey

| question | options | recommended | rationale |
| --- | --- | --- | --- |
| Q1 (decisive) — The PRD says F-7 sends via 'the existing local rectify MCP server', but a repo-wide grep of every *.kt/*.kts/*.swift/*.toml file found zero MCP server/client/JSON-RPC code anywhere in source — every match is .pipeline/ prose. What should F-7 actually build? | ["Build a full, in-repo MCP server (new module, desktop-hosted/spawned) — largest scope, self-contained, matches 'sole transport in v1' literally.", "Build client-side publishing only, targeting a separately-maintained external 'rectify' MCP server the user already runs — smaller repo scope, but introduces an external contract this repo cannot test end to end.", "Ship the payload-publication seam plus a bounded in-repo stdio MCP server stub sufficient to prove AC-18/AC-81, deferring full protocol hardening (multi-client, auth, discovery) to a follow-up."] | Ship the payload-publication seam plus a bounded in-repo stdio MCP server stub sufficient to prove AC-18/AC-81, deferring full protocol hardening (multi-client, auth, discovery) to a follow-up. | No MCP SDK, JSON-RPC library, or server dependency exists in gradle/libs.versions.toml (ktor-client-* is cataloged but used in zero build.gradle.kts files; there is no ktor-server-* entry at all), and no convention plugin models a standalone server process — build-logic/convention only has application/feature/library/di. :desktop-application's main() -> initKoin() -> runDesktopApp() is the only platform entry point that could plausibly host a spawned local process, since Android/iOS can't run a dev-machine-local MCP server. Treating 'the existing server' as literally true (option 2) would make this repo depend on an external contract nothing here can verify, violating NFR-4's local-only guarantee in spirit if not in letter. Building full production hardening (option 1) contradicts the 'fast' effort tier and this repo's own pattern of shipping the minimum that satisfies the AC text: AC-18 only requires 'a Claude Code session calling the retrieval tool receives the complete payload', which a bounded stdio stub satisfies without inventing multi-session/auth machinery nothing asked for. |
| Q2 — What is the exact name of the MCP retrieval tool the agent calls? | ["get_latest_annotation (literal PRD/FR-17 wording)", "resolve_annotation (Aki's nearest precedent, per research.json)", "rectify_get_latest_annotation (namespaced to avoid collision with any other local MCP server's tool names)"] | get_latest_annotation | FR-17 and AC-18 already name this tool in the PRD-approved text ('a get_latest_annotation-style tool'); using it verbatim keeps the clearest trace from PRD to contract and avoids a second unnecessary naming decision. Namespacing (option 3) is worth revisiting only if a real naming collision shows up once an actual Claude Code MCP config is tested, which is tracked as an openQuestion rather than blocking now. |
| Q3 — What transport does the in-repo MCP server use? | ["stdio subprocess (Claude Code spawns the server process via its MCP config, communicates over stdin/stdout)", "localhost TCP socket (port TBD)", "localhost HTTP (port TBD)"] | stdio subprocess | stdio needs zero new dependencies (kotlinx-serialization + System.in/out), matches how most local MCP servers (including Claude Code's own MCP client convention) are configured and spawned, and trivially satisfies NFR-4's 'no network egress' since no socket is ever opened. TCP/HTTP would require adding a new ktor-server-* catalog entry that does not exist today, for a guarantee (localhost-only) that stdio already provides for free. |
| Q4 — What does the toast's 'Open session' link do on desktop? | ["Copy the current session identifier to the system clipboard", "Attempt to focus/activate a running Claude Code application window", "Open a web browser to a session URL", "No-op with an explanatory tooltip (deferred)"] | Copy the current session identifier to the system clipboard | Nothing in this codebase or its dependencies can focus an external application window or knows a session URL scheme for Claude Code (a CLI-first tool); clipboard copy uses Compose's existing clipboard API, needs no new dependency or platform-specific code, and gives the user a concrete, pasteable artifact to use in their own Claude Code terminal. |

### survey

| question | options | answer | source |
| --- | --- | --- | --- |
| Q1 (decisive) — The PRD says F-7 sends via 'the existing local rectify MCP server', but a repo-wide grep of every *.kt/*.kts/*.swift/*.toml file found zero MCP server/client/JSON-RPC code anywhere in source. What should F-7 actually build? | ["Ship the payload-publication seam plus a bounded in-repo stdio MCP server stub sufficient to prove AC-18/AC-81, deferring full protocol hardening (multi-client, auth, discovery) to a follow-up.", "Build a full, in-repo MCP server (new module, desktop-hosted/spawned).", "Build client-side publishing only, targeting a separately-maintained external 'rectify' MCP server."] | Ship the payload-publication seam plus a bounded in-repo stdio MCP server stub sufficient to prove AC-18/AC-81, deferring full protocol hardening (multi-client, auth, discovery) to a follow-up. | user-edited |
| Q2 — What is the exact name of the MCP retrieval tool the agent calls? | ["get_latest_annotation (literal PRD/FR-17 wording)", "resolve_annotation (Aki's precedent)", "rectify_get_latest_annotation (namespaced)"] | get_latest_annotation (literal PRD/FR-17 wording) | user-edited |
| Q3 — What transport does the in-repo MCP server use? | ["stdio subprocess (Claude Code spawns the server process; stdin/stdout)", "localhost TCP socket (port TBD)", "localhost HTTP (port TBD)"] | stdio subprocess (Claude Code spawns the server process; stdin/stdout) | user-edited |
| Q4 — What does the toast's 'Open session' link do on desktop? | ["Copy the current session identifier to the system clipboard", "Attempt to focus/activate a running Claude Code application window", "Open a web browser to a session URL", "No-op with an explanatory tooltip (deferred)"] | Copy the current session identifier to the system clipboard | user-edited |
