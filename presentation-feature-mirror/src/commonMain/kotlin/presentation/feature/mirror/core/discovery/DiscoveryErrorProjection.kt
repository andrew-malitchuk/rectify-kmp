package presentation.feature.mirror.core.discovery

import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.PlatformModel
import presentation.feature.mirror.source.mirror.HierarchyStatus
import presentation.feature.mirror.source.mirror.MirrorDropBanner
import presentation.core.ui.source.kit.atom.indicator.StatusIndicatorState
import presentation.feature.mirror.source.mirror.MirrorErrorCard
import presentation.feature.mirror.source.mirror.MirrorRowGroup
import presentation.feature.mirror.source.mirror.MirrorErrorKind
import presentation.feature.mirror.source.mirror.MirrorIntent
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorUiState

/**
 * The command the "Restart adb" action reports and runs — `kill-server` then a fresh `devices`.
 *
 * Machine text, deliberately unlocalised, and a constant rather than a literal so the line the
 * card shows cannot drift from the one the app runs — the handler behind the card's "Restart adb"
 * primary is its second reader, and a button whose label and effect disagree is the defect
 * [MirrorIntent.RestartAdb] exists to avoid.
 */
internal const val ADB_RESTART_COMMAND = "adb kill-server && adb devices"

/** Binary behind every Android state here; [DiscoveryModel.NoDeviceFound] carries no name. */
private const val ADB = "adb"

/** Binary behind the iOS simulator states - the Xcode shim, not `simctl`. */
private const val XCRUN = "xcrun"

/** The exact line that lists physical iOS targets, reported by the not-trusted card. */
internal const val IDB_LIST_TARGETS_COMMAND = "idb list-targets"

/** Prefix of the line the `err-sim` card shows; the udid is appended by the projection. */
internal const val SIMCTL_BOOT_COMMAND = "xcrun simctl boot"

/** Separator used by every machine line in this design language — same as the status line's. */
private const val SEPARATOR = " · "

/** Tenths of a second, for the poll-age line. */
private const val TENTHS_PER_SECOND = 10L

/** Milliseconds per tenth of a second. */
private const val MS_PER_TENTH = 100L

/** Half a tenth, added before the integer division so the age rounds instead of truncating. */
private const val MS_ROUNDING = 50L

/** Fewest usable handsets that make the choice the user's rather than ours (FR-34). */
private const val AMBIGUOUS_DEVICE_COUNT = 2

/**
 * Projects the actionable error card out of the reduced state (AC-26…AC-29, AC-53).
 *
 * Pure and Compose-free on purpose: `commonTest` is the only harness this module has, so the
 * decision of *which* card appears has to be assertable without rendering anything. Same
 * precedent as [applyDiscovery] next door — the ViewModel keeps no derived error state.
 *
 * Returns `null` whenever something else owns the stage. The picker is a decision the user is
 * already making, a live or pinned frame is pixels they are already looking at, and a probe in
 * flight has not learned anything yet; the `null` for those is also what keeps `NoSignalStage`
 * (AC-4) reachable instead of papering a card over it.
 *
 * @param nowMs Wall clock for the poll-age line, passed in rather than read from
 *   `Clock.System` so the rendered diagnostic is deterministic under test.
 * @return The card to compose, or `null` when this state has no actionable error.
 */
internal fun MirrorUiState.errorCard(nowMs: Long): MirrorErrorCard? {
    if (session is MirrorSession.Streaming || frozen != null) return null
    // The picker wins wherever it still has something to offer. For `Found` that is the device
    // list; for `PollFailed` it is the list it DELIBERATELY retained — applyDiscovery keeps the
    // rows on a failed poll "so an adb hiccup does not look like an unplug", and papering a
    // full-stage card over them made those retained rows unreachable for exactly as long as
    // they were retained, flipping the stage back and forth once per poll on a flaky daemon
    // (COR-6).
    //
    // ToolMissing and NoDeviceFound are different: there the picker has nothing to list, so
    // yielding to it would hide the one surface that explains why.
    //
    // The two iOS variants join Found for the same reason, and this is load-bearing rather
    // than tidy: both cards offer a secondary that OPENS the picker, and errorCard() is
    // computed ahead of the stage `when`, so without yielding here the card would re-cover
    // the picker it just opened and "Choose another" / "Use simulator" would be dead buttons.
    // DeviceNotTrusted yields only when a simulator actually exists - with nothing to list,
    // the picker would be an empty panel over the one surface explaining the problem.
    if (isPickerOpen && discovery.yieldsToPicker()) return null
    return when (val outcome = discovery) {
        DiscoveryModel.Detecting -> null

        is DiscoveryModel.ToolMissing -> MirrorErrorCard(
            kind = MirrorErrorKind.ToolMissing,
            platform = platform,
            command = outcome.installCommand,
            // Every path that was tried, then the env hint: "why didn't it find mine" is the
            // question this state exists to answer, and a summarised list cannot answer it.
            diagnostic = (outcome.probedPaths + outcome.envHint).joinToString(SEPARATOR),
        )

        is DiscoveryModel.NoDeviceFound -> MirrorErrorCard(
            kind = MirrorErrorKind.NoDevice,
            platform = platform,
            // There is no iOS equivalent of restarting the adb daemon, and a command block
            // naming adb on an iOS bench would be machine text that is simply false.
            command = ADB_RESTART_COMMAND.takeIf { platform == PlatformModel.Android },
            diagnostic = listOf(
                "${discoveryTool()} ${outcome.toolVersion}",
                "last poll ${formatAge(nowMs - outcome.lastPollEpochMs)} ago",
                "${outcome.attachedCount} attached",
            ).joinToString(SEPARATOR),
            // Offering "Boot emulator instead" with no AVD configured would be a dead button.
            canBootEmulator = avds.isNotEmpty(),
        )

        // A separate kind and a visibly different diagnostic from NoDeviceFound, because the
        // two mean opposite things: that one looked and saw nothing, this one never got an
        // answer. It carries no command — there is nothing to re-run but the poll itself.
        is DiscoveryModel.PollFailed -> MirrorErrorCard(
            kind = MirrorErrorKind.PollFailed,
            platform = platform,
            diagnostic = listOf("${outcome.tool} poll failed", outcome.reason).joinToString(SEPARATOR),
        )

        is DiscoveryModel.Found -> multipleDevices(outcome)

        is DiscoveryModel.SimulatorNotBooted -> MirrorErrorCard(
            kind = MirrorErrorKind.SimulatorNotBooted,
            platform = PlatformModel.Ios,
            // Addressed by udid, never by name: the captured fixture has two `iPhone 15 Pro`
            // under one runtime, so the name does not identify what this button would boot.
            command = outcome.bootTarget()?.let { "$SIMCTL_BOOT_COMMAND $it" },
            diagnostic = listOf(
                "$XCRUN ${outcome.toolVersion}",
                "last poll ${formatAge(nowMs - outcome.lastPollEpochMs)} ago",
                "${outcome.simulators.size} shut down",
            ).joinToString(SEPARATOR),
            primaryDeviceId = outcome.bootTarget(),
        )

        is DiscoveryModel.DeviceNotTrusted -> MirrorErrorCard(
            kind = MirrorErrorKind.DeviceNotTrusted,
            platform = PlatformModel.Ios,
            command = IDB_LIST_TARGETS_COMMAND,
            diagnostic = listOf(outcome.deviceName, outcome.deviceId).joinToString(SEPARATOR),
            primaryDeviceId = outcome.deviceId,
            // Reused verbatim for "is there something else to fall back to": the secondary
            // label differs, the question does not, and a second boolean saying the same
            // thing is a second thing to get out of sync.
            canBootEmulator = outcome.simulatorsAvailable,
        )
    }
}

/**
 * Whether the picker, when open, outranks the card this discovery state would produce.
 *
 * `Found` is the device list itself; `PollFailed` is the list applyDiscovery DELIBERATELY
 * retained "so an adb hiccup does not look like an unplug" (COR-6); the two iOS states are the
 * ones whose own secondary action opens the picker.
 */
private fun DiscoveryModel.yieldsToPicker(): Boolean = when (this) {
    is DiscoveryModel.Found, is DiscoveryModel.PollFailed -> true
    is DiscoveryModel.SimulatorNotBooted -> true
    is DiscoveryModel.DeviceNotTrusted -> simulatorsAvailable
    DiscoveryModel.Detecting, is DiscoveryModel.ToolMissing, is DiscoveryModel.NoDeviceFound -> false
}

/** The simulator the card's primary would boot: the first one listed, by udid. */
private fun DiscoveryModel.SimulatorNotBooted.bootTarget(): String? =
    simulators.firstNotNullOfOrNull { it.runningDeviceId }

/**
 * Binary named in the diagnostic footer of the SHARED cards.
 *
 * `xcrun` rather than `simctl` on iOS, matching what the probe actually resolved and what the
 * install hint tells the user to repair.
 */
private fun MirrorUiState.discoveryTool(): String = when (platform) {
    PlatformModel.Android -> ADB
    PlatformModel.Ios -> XCRUN
}

/** The exact listing command the multi-device card's footer reports, per platform. */
private fun MirrorUiState.listDevicesCommand(): String = when (platform) {
    PlatformModel.Android -> "$ADB devices -l"
    PlatformModel.Ios -> IDB_LIST_TARGETS_COMMAND
}

/**
 * The ambiguous-choice card, or `null` when there is nothing ambiguous about the choice.
 *
 * One usable handset is not an error — it is what the picker is for. Two or more is, because
 * nothing but the user can say which one they meant.
 */
private fun MirrorUiState.multipleDevices(outcome: DiscoveryModel.Found): MirrorErrorCard? {
    if (outcome.physical.count(DeviceModel::isSelectable) < AMBIGUOUS_DEVICE_COUNT) return null
    // COR-4. PHYSICAL rows only. `rows` is physical + emulators, and a configured-but-cold
    // AVD is `selectable = true, kind = Boot` — so listing it here put three disagreeing
    // numbers on screen (title "4 devices connected", footer "3 attached", status line
    // "2 devices attached") and, far worse, made it clickable under body copy reading "Pick one
    // to mirror": `commit()` routes a Boot row to `boot()`, so the card LAUNCHED AN EMULATOR
    // instead of mirroring. AC-28 asks for the attached devices, and a cold AVD is not
    // attached.
    val attached = rows.filter { it.group == MirrorRowGroup.Physical }
    return MirrorErrorCard(
        kind = MirrorErrorKind.MultipleDevices,
        platform = platform,
        // Unauthorised ones stay, shown disabled rather than hidden: a user holding the phone
        // they just plugged in must see it somewhere, even if they cannot pick it yet.
        rows = attached,
        primaryDeviceId = attached.firstOrNull { it.selectable }?.id,
        diagnostic = listOf(listDevicesCommand(), "${outcome.physical.size} attached").joinToString(SEPARATOR),
        canBootEmulator = avds.isNotEmpty(),
    )
}

/**
 * Projects the stream-drop banner (AC-29 / AS-17).
 *
 * Non-null for exactly one state — the device went away while a frame was pinned — which is the
 * only state where [presentation.feature.mirror.source.mirror.MirrorIntent.Reconnect] has
 * somewhere to reconnect to and work worth keeping.
 *
 * The count is reported verbatim, zero included: no new counting logic, and no zero-suppression
 * either — the design's banner always states the number, so deciding not to show it is the
 * content layer's business and not this projection's.
 *
 * @return The banner to compose, or `null` when the stream is not in a dropped-while-frozen state.
 */
internal fun MirrorUiState.dropBanner(): MirrorDropBanner? {
    val lost = session as? MirrorSession.Lost ?: return null
    val pinned = frozen ?: return null
    return MirrorDropBanner(deviceId = lost.deviceId, annotationCount = pinned.annotations.size)
}

/** One decimal place by hand, the way `StatsFormatter` does it — no common-stdlib formatter. */
private fun formatAge(ageMs: Long): String {
    val tenths = (ageMs.coerceAtLeast(0) + MS_ROUNDING) / MS_PER_TENTH
    return "${tenths / TENTHS_PER_SECOND}.${tenths % TENTHS_PER_SECOND} s"
}

/**
 * The status-line dot, as a pure function so it is assertable without a UI harness.
 *
 * Lives here rather than beside the composable that renders it because the design pairs the
 * status line with the error card state by state — `err-adb` is "adb not found in PATH",
 * `err-nodevice` is "adb devices -> 0 attached", `err-multi` is "N devices attached, waiting
 * for choice" — so the two projections have to agree, and the only way to keep them agreeing
 * is to put them where one test can read both.
 *
 * Without this, the status bar went on saying "Waiting for device…" while the card next to it
 * said adb was missing entirely.
 *
 * Order matters: a pinned frame outranks a live session (FR-30 — the session really is running
 * underneath, but a LIVE dot over a still frame is a lie), and both outrank any discovery
 * state, because what the user is looking at is more informative than what the poll last said.
 */
internal fun MirrorUiState.indicatorDot(card: MirrorErrorCard?): StatusIndicatorState = when {
    // Above the frozen case, and only there: a dump in flight is the one state that costs the
    // user ~2.7 s of waiting, and a FROZEN dot over it says "nothing is happening".
    hierarchyStatus == HierarchyStatus.Dumping -> StatusIndicatorState.Dumping
    frozen != null -> StatusIndicatorState.Frozen
    session is MirrorSession.Streaming -> StatusIndicatorState.Live
    session is MirrorSession.Lost -> StatusIndicatorState.Dropped
    // Blocked, not Offline: each of these is waiting on a person, and a grey dot reads as
    // idle. The ambiguous-choice case is Blocked too - two usable handsets is not "no device".
    discovery is DiscoveryModel.ToolMissing -> StatusIndicatorState.Blocked
    discovery is DiscoveryModel.PollFailed -> StatusIndicatorState.Blocked
    discovery is DiscoveryModel.NoDeviceFound -> StatusIndicatorState.Blocked
    // The card is PASSED IN rather than recomputed. It used to call errorCard(nowMs = 0L),
    // which both duplicated the work and quietly asked the projection a question with a
    // fabricated clock — harmless for the kind, but the kind of shortcut that stops being
    // harmless the moment the projection starts reading nowMs for something else.
    card?.kind == MirrorErrorKind.MultipleDevices -> StatusIndicatorState.Blocked
    else -> StatusIndicatorState.Offline
}
