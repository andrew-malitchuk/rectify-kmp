package presentation.feature.mirror

import androidx.compose.ui.unit.IntSize
import domain.core.source.model.device.DeviceModel
import domain.core.source.model.device.DeviceStateModel
import domain.core.source.model.device.DiscoveryModel
import domain.core.source.model.device.EmulatorModel
import presentation.feature.mirror.core.discovery.ADB_RESTART_COMMAND
import presentation.feature.mirror.core.discovery.applyDiscovery
import presentation.feature.mirror.core.discovery.dropBanner
import presentation.core.ui.source.kit.atom.indicator.StatusIndicatorState
import presentation.feature.mirror.core.discovery.indicatorDot
import presentation.feature.mirror.core.discovery.errorCard
import presentation.feature.mirror.source.mirror.DeviceRect
import presentation.feature.mirror.source.mirror.FrozenFrame
import presentation.feature.mirror.source.mirror.MirrorDropBanner
import presentation.feature.mirror.source.mirror.MirrorErrorKind
import presentation.feature.mirror.source.mirror.MirrorSession
import presentation.feature.mirror.source.mirror.MirrorUiState
import presentation.feature.mirror.source.mirror.StagedAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/** Fixed wall clock — the whole point of `errorCard`'s `nowMs` parameter (TAC-4). */
private const val NOW = 1_760_000_000_000L

private const val PIXEL = "37281FDH2000AB"
private const val GALAXY = "RF8M90ABCDE"
private const val PENDING = "0A1B2C3D"
private const val AVD = "Pixel_Fold_API_35"

/** The mono separator every machine line in this design language uses. */
private const val SEPARATOR = " · "

private fun device(
    id: String,
    name: String,
    state: DeviceStateModel = DeviceStateModel.Device,
): DeviceModel = DeviceModel(
    id = id,
    displayName = name,
    serial = id,
    state = state,
    isEmulator = false,
    androidVersion = "15",
    transportLabel = "USB",
)

private fun toolMissing(): DiscoveryModel.ToolMissing = DiscoveryModel.ToolMissing(
    tool = "adb",
    probedPaths = listOf("/usr/local/bin/adb", "/opt/homebrew/bin/adb", "/Users/x/Library/Android/sdk/platform-tools/adb"),
    installCommand = "brew install --cask android-platform-tools",
    envHint = "\$ANDROID_HOME unset",
)

private fun noDeviceFound(attachedCount: Int = 1): DiscoveryModel.NoDeviceFound =
    DiscoveryModel.NoDeviceFound(
        toolVersion = "35.0.1",
        lastPollEpochMs = NOW - 800,
        attempts = 3,
        attachedCount = attachedCount,
    )

private fun pollFailed(): DiscoveryModel.PollFailed = DiscoveryModel.PollFailed(
    tool = "adb",
    reason = "adb server version (40) doesn't match this client (41)",
)

/**
 * The state the card is projected out of, with the picker closed.
 *
 * Built through the real [applyDiscovery] rather than by hand-writing [MirrorUiState.rows]: the
 * row list the card carries is the picker's own, and a hand-built one could agree with the card
 * while disagreeing with what the picker would show.
 */
/** Two usable handsets — the ambiguity the picker exists to resolve. */
private fun twoSelectablePhones(): DiscoveryModel.Found = DiscoveryModel.Found(
    physical = listOf(device(PIXEL, "Pixel 8 Pro"), device(GALAXY, "Galaxy S24")),
    emulators = emptyList(),
)

private fun stateOf(discovery: DiscoveryModel): MirrorUiState =
    MirrorUiState().applyDiscovery(discovery).copy(isPickerOpen = false)

/**
 * Names each check so ONE [assertEquals] reports every clause.
 *
 * A chain of `assertTrue`/`assertFalse` short-circuits on the first failure, which is how a
 * mutation that only kills one clause looks as if it killed them all — the exact correction the
 * T-2/T-3 executor recorded against T1b's mutation clause.
 */
private fun verdicts(vararg results: Pair<String, Any?>): List<String> =
    results.map { (name, value) -> "$name=$value" }

class DiscoveryErrorProjectionTest {

    /**
     * T5 (AC-26) — kind, the install command verbatim, and a diagnostic naming every path tried.
     *
     * MUTATION: drop probedPaths from the diagnostic builder — the contains-every-path
     * assertion must fail.
     */
    @Test
    fun toolMissingProjectsTheInstallCommandCard() {
        val probe = toolMissing()
        val card = assertNotNull(stateOf(probe).errorCard(NOW))

        assertEquals(
            expected = verdicts(
                "kind" to MirrorErrorKind.ToolMissing,
                "command" to "brew install --cask android-platform-tools",
                // "why didn't it find mine" is the question this state exists to answer, so
                // every probed path has to be in the footer, not a summary of them.
                "missingFromDiagnostic" to emptyList<String>(),
            ),
            actual = verdicts(
                "kind" to card.kind,
                "command" to card.command,
                "missingFromDiagnostic" to (probe.probedPaths + probe.envHint)
                    .filterNot { it in card.diagnostic },
            ),
        )
    }

    /**
     * T9 (AC-27) — the version that answered, the derived poll age and the attached count.
     *
     * MUTATION: drop attachedCount from the diagnostic — the contains assertion must fail.
     * The diagnostic is asserted as its exact split, so dropping ANY of the three segments
     * fails here rather than only the one the clause names.
     */
    @Test
    fun noDeviceFoundProjectsVersionLastPollAndAttachedCount() {
        val card = assertNotNull(stateOf(noDeviceFound()).errorCard(NOW))

        assertEquals(
            expected = verdicts(
                "kind" to MirrorErrorKind.NoDevice,
                "command" to ADB_RESTART_COMMAND,
                "diagnostic" to listOf("adb 35.0.1", "last poll 0.8 s ago", "1 attached"),
                // No AVD is known, so "Boot emulator instead" would be a dead button.
                "canBootEmulator" to false,
            ),
            actual = verdicts(
                "kind" to card.kind,
                "command" to card.command,
                "diagnostic" to card.diagnostic.split(SEPARATOR),
                "canBootEmulator" to card.canBootEmulator,
            ),
        )
        assertEquals("adb kill-server && adb devices", ADB_RESTART_COMMAND)
    }

    /**
     * T6 (AC-53 / FR-32) — a poll that never answered is not a poll that saw nothing.
     *
     * MUTATION: map PollFailed through the NoDeviceFound branch — both the inequality and the
     * kind assertion must fail.
     */
    @Test
    fun pollFailedProjectsItsOwnCardDistinctFromNoDeviceFound() {
        val failed = pollFailed()
        val failedCard = assertNotNull(stateOf(failed).errorCard(NOW))
        val emptyCard = assertNotNull(stateOf(noDeviceFound(attachedCount = 0)).errorCard(NOW))

        assertNotEquals(emptyCard, failedCard)
        assertEquals(
            expected = verdicts(
                "kind" to MirrorErrorKind.PollFailed,
                "reasonInDiagnostic" to true,
                // Nothing to re-run but the poll itself, so the card carries no command block.
                "command" to null,
                "distinctKind" to true,
            ),
            actual = verdicts(
                "kind" to failedCard.kind,
                "reasonInDiagnostic" to (failed.reason in failedCard.diagnostic),
                "command" to failedCard.command,
                "distinctKind" to (failedCard.kind != emptyCard.kind),
            ),
        )
    }

    /**
     * T7 (AC-28 / FR-34) — one row per attached device, the unauthorised one shown disabled
     * rather than hidden, and the top usable device as the one-click primary.
     *
     * MUTATION A: hardcode primaryDeviceId = null — the primary assertion fails.
     * MUTATION B: filter unusable rows out of `rows` — the disabled-row-present assertion fails.
     */
    @Test
    fun threeDevicesProjectOneRowPerDeviceAndATopDevicePrimary() {
        val found = DiscoveryModel.Found(
            physical = listOf(
                device(PIXEL, "Pixel 8 Pro"),
                device(GALAXY, "Galaxy S24"),
                device(PENDING, PENDING, state = DeviceStateModel.Unauthorized),
            ),
            emulators = emptyList(),
        )
        val card = assertNotNull(stateOf(found).errorCard(NOW))
        // Dropping the first handset leaves one usable device beside the unauthorised one.
        val onlyOneUsable = found.copy(physical = found.physical.drop(1))
        val withAvd = found.copy(emulators = listOf(EmulatorModel(avdName = AVD, isRunning = false)))

        assertEquals(
            expected = verdicts(
                "kind" to MirrorErrorKind.MultipleDevices,
                "rowIds" to listOf(PIXEL, GALAXY, PENDING),
                "selectable" to listOf(true, true, false),
                "primaryDeviceId" to PIXEL,
                "command" to null,
                "canBootEmulator" to false,
                "canBootEmulatorWithAvd" to true,
                // One usable handset is not ambiguous — that is what the picker is for.
                "oneUsableDevice" to null,
            ),
            actual = verdicts(
                "kind" to card.kind,
                "rowIds" to card.rows.map { it.id },
                "selectable" to card.rows.map { it.selectable },
                "primaryDeviceId" to card.primaryDeviceId,
                "command" to card.command,
                "canBootEmulator" to card.canBootEmulator,
                "canBootEmulatorWithAvd" to stateOf(withAvd).errorCard(NOW)?.canBootEmulator,
                "oneUsableDevice" to stateOf(onlyOneUsable).errorCard(NOW)?.kind,
            ),
        )
    }

    /**
     * T10 — the card yields the stage to whatever the user is already looking at (TAC-1).
     *
     * This null is also what keeps AC-4's passive NO SIGNAL stage reachable instead of papering
     * a card over it.
     *
     * REVISED in T-14, and the reason matters. The guard was originally
     * `if (isPickerOpen) return null` for every state, which made three of the four card kinds
     * **unreachable**: the chosen plan routes the card from the `NoSignal` branch only, and of
     * the four discovery states just `NoDeviceFound` closes the picker. F-2 deliberately keeps
     * it open for `ToolMissing` and `PollFailed` — its own tests assert `stage == Picker` and
     * those are confirmed ACs — so AC-26's and AC-53's cards could never render at all.
     *
     * The picker now wins only for `Found`, where it is the genuine multi-device affordance
     * carrying the keyboard navigation AC-43 and A11Y-6 were confirmed against. For the three
     * failure states it has nothing to show, so the card takes the stage.
     *
     * MUTATION A: drop the `discovery is Found` qualifier, i.e. yield to ANY open picker — the
     * toolMissingWithPickerOpen assertion fails, which is the regression that made those three
     * states dead UI.
     * MUTATION B: remove the session guard — the streaming assertion fails.
     */
    @Test
    fun errorCardIsNullWhileThePickerIsOpenOrASessionOwnsTheStage() {
        val base = stateOf(noDeviceFound())
        val pinned = FrozenFrame(
            deviceSize = IntSize(1080, 2400),
            annotations = listOf(StagedAnnotation(index = 1, rect = DeviceRect(10, 20, 30, 40))),
        )

        assertEquals(
            expected = verdicts(
                "foundWithPickerOpen" to null,
                "toolMissingWithPickerOpen" to MirrorErrorKind.ToolMissing,
                "streaming" to null,
                "frozen" to null,
                "detecting" to null,
                "otherwise" to MirrorErrorKind.NoDevice,
            ),
            actual = verdicts(
                // The picker is the real affordance here, so the card stands down.
                "foundWithPickerOpen" to base.copy(
                    isPickerOpen = true,
                    discovery = twoSelectablePhones(),
                ).errorCard(NOW)?.kind,
                // The picker has nothing to list, so the card must still be reachable - this
                // is the assertion that stops AC-26 becoming dead UI again.
                "toolMissingWithPickerOpen" to base.copy(
                    isPickerOpen = true,
                    discovery = toolMissing(),
                ).errorCard(NOW)?.kind,
                "streaming" to base.copy(session = MirrorSession.Streaming(PIXEL)).errorCard(NOW)?.kind,
                "frozen" to base.copy(frozen = pinned).errorCard(NOW)?.kind,
                "detecting" to base.copy(discovery = DiscoveryModel.Detecting).errorCard(NOW)?.kind,
                "otherwise" to base.errorCard(NOW)?.kind,
            ),
        )
    }

    /**
     * T8 (AC-29 / AS-17) — the banner reports how much work the drop did NOT destroy.
     *
     * The zero case is asserted as `0` and not as an absent banner: per
     * `inputs/design-error-states.md` CORRECTION 1 the design's banner always states the count,
     * so suppressing it is not this projection's business (T-16 renders it unconditionally).
     *
     * MUTATION: hardcode annotationCount = 0 — the count assertion must fail.
     */
    @Test
    fun streamDropBannerReportsTheStagedCount() {
        val lost = MirrorUiState(
            session = MirrorSession.Lost(PIXEL),
            frozen = FrozenFrame(
                deviceSize = IntSize(1080, 2400),
                annotations = listOf(
                    StagedAnnotation(index = 1, rect = DeviceRect(10, 20, 30, 40)),
                    StagedAnnotation(index = 2, rect = DeviceRect(50, 60, 70, 80), comment = "cut off"),
                ),
            ),
        )

        assertEquals(
            expected = listOf(MirrorDropBanner(PIXEL, 2), MirrorDropBanner(PIXEL, 0)),
            actual = listOf(
                lost.dropBanner(),
                lost.copy(frozen = lost.frozen?.copy(annotations = emptyList())).dropBanner(),
            ),
        )
    }

    /**
     * T8b — non-null for exactly one state: the device went away while a frame was pinned.
     *
     * MUTATION: drop the `session is Lost` condition — the streaming assertion must fail.
     */
    @Test
    fun dropBannerIsNullWhileTheSessionIsStreaming() {
        val pinned = FrozenFrame(deviceSize = IntSize(1080, 2400))
        val lost = MirrorUiState(session = MirrorSession.Lost(PIXEL), frozen = pinned)

        assertEquals(
            expected = verdicts(
                "lostWithFrozen" to MirrorDropBanner(PIXEL, 0),
                "streamingWithFrozen" to null,
                "lostWithoutFrozen" to null,
                "idle" to null,
            ),
            actual = verdicts(
                "lostWithFrozen" to lost.dropBanner(),
                "streamingWithFrozen" to lost.copy(session = MirrorSession.Streaming(PIXEL)).dropBanner(),
                "lostWithoutFrozen" to lost.copy(frozen = null).dropBanner(),
                "idle" to MirrorUiState(frozen = pinned).dropBanner(),
            ),
        )
    }

    /**
     * T-32 — the status dot agrees with the card beside it.
     *
     * The design pairs them state by state. Before this, the bar said "Waiting for device…"
     * next to a card explaining that adb was missing entirely, and the dot stayed grey —
     * which reads as "nothing is happening" for three states that are all waiting on a person.
     *
     * `Blocked` exists for exactly that distinction and is asserted here rather than trusted:
     * reusing `Offline` would have compiled and looked fine.
     *
     * MUTATION A: map ToolMissing to Offline — the toolMissing clause fails.
     * MUTATION B: drop the MultipleDevices clause — the twoPhones clause fails.
     * MUTATION C: move `frozen` below `discovery` — the frozenOverridesDiscovery clause fails.
     * No single mutation covers all three, so each clause is load-bearing.
     */
    @Test
    fun theStatusDotMatchesTheCardItSitsBeside() {
        val pinned = FrozenFrame(
            deviceSize = IntSize(1080, 2400),
            annotations = listOf(StagedAnnotation(index = 1, rect = DeviceRect(10, 20, 30, 40))),
        )

        assertEquals(
            expected = verdicts(
                "toolMissing" to StatusIndicatorState.Blocked,
                "pollFailed" to StatusIndicatorState.Blocked,
                "noDevice" to StatusIndicatorState.Blocked,
                "twoPhones" to StatusIndicatorState.Blocked,
                "detecting" to StatusIndicatorState.Offline,
                "streaming" to StatusIndicatorState.Live,
                "lost" to StatusIndicatorState.Dropped,
                "frozenOverridesDiscovery" to StatusIndicatorState.Frozen,
            ),
            actual = verdicts(
                "toolMissing" to stateOf(toolMissing()).indicatorDot(),
                "pollFailed" to stateOf(pollFailed()).indicatorDot(),
                "noDevice" to stateOf(noDeviceFound(attachedCount = 0)).indicatorDot(),
                "twoPhones" to stateOf(twoSelectablePhones()).indicatorDot(),
                "detecting" to stateOf(DiscoveryModel.Detecting).indicatorDot(),
                "streaming" to stateOf(noDeviceFound())
                    .copy(session = MirrorSession.Streaming(PIXEL)).indicatorDot(),
                "lost" to stateOf(noDeviceFound())
                    .copy(session = MirrorSession.Lost(PIXEL)).indicatorDot(),
                // A pinned frame wins over a discovery state that would otherwise say Blocked:
                // a LIVE or BLOCKED dot over a still frame misreports what is on screen.
                "frozenOverridesDiscovery" to stateOf(toolMissing())
                    .copy(frozen = pinned).indicatorDot(),
            ),
        )
    }
}
