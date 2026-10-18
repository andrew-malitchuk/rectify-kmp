package presentation.core.ui.source.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import presentation.core.styling.core.Theme
import presentation.core.ui.source.kit.atom.button.AppButton
import presentation.core.ui.source.kit.atom.button.AppButtonSize
import presentation.core.ui.source.kit.atom.button.AppButtonVariant
import presentation.core.ui.source.kit.atom.chip.Chip
import presentation.core.ui.source.kit.atom.icon.Home
import presentation.core.ui.source.kit.atom.icon.RefreshCcw
import presentation.core.ui.source.kit.atom.icon.Settings
import presentation.core.ui.source.kit.atom.icon.Wifi
import presentation.core.ui.core.modifier.cascadeIn
import presentation.core.ui.source.kit.atom.indicator.StatusIndicator
import presentation.core.ui.source.kit.atom.indicator.StatusIndicatorState
import presentation.core.ui.source.kit.atom.input.Input
import presentation.core.ui.source.kit.atom.progress.SegmentedBars
import presentation.core.ui.source.kit.atom.progress.SegmentedProgressIndicator
import presentation.core.ui.source.kit.atom.slider.WavySlider
import presentation.core.ui.source.kit.atom.tile.BentoTile
import presentation.core.ui.source.kit.atom.tile.BentoTileHeight
import presentation.core.ui.source.kit.atom.tile.BentoTileVariant
import presentation.core.ui.source.kit.atom.toggle.SimpleToggle
import presentation.core.ui.source.kit.molecule.bar.tab.TabBar
import presentation.core.ui.source.kit.molecule.bar.tab.TabBarItem
import presentation.core.ui.source.kit.atom.annotation.AnnotationMark
import presentation.core.ui.source.kit.atom.annotation.AnnotationState
import presentation.core.ui.source.kit.atom.annotation.MeasureMark
import presentation.core.ui.source.kit.atom.annotation.MeasureMarkRole
import presentation.core.ui.source.kit.atom.badge.CounterBadge
import presentation.core.ui.source.kit.atom.badge.CounterBadgeState
import presentation.core.ui.source.kit.atom.glyph.ErrorGlyph
import presentation.core.ui.source.kit.atom.glyph.ErrorKind
import presentation.core.ui.source.kit.atom.tag.MeasureTag
import presentation.core.ui.source.kit.organism.error.ErrorCard
import presentation.core.ui.source.kit.molecule.annotation.AnnotationOverlay
import presentation.core.ui.source.kit.molecule.annotation.AnnotationOverlayMark
import presentation.core.ui.source.kit.molecule.annotation.AnnotationPopover
import presentation.core.ui.source.kit.molecule.annotation.Ruler
import presentation.core.ui.source.kit.organism.annotation.CommentPopover
import presentation.core.ui.source.kit.organism.annotation.MeasureCard
import presentation.core.ui.source.kit.organism.annotation.MeasureSpec
import presentation.core.ui.source.kit.atom.keycap.KeyCap
import presentation.core.ui.source.kit.molecule.bar.tab.SidebarTabItem
import presentation.core.ui.source.kit.molecule.bar.tab.SidebarTabs
import presentation.core.ui.source.kit.molecule.button.SegmentedButtonGroup
import presentation.core.ui.source.kit.molecule.chip.DeviceChip
import presentation.core.ui.source.kit.molecule.chip.DeviceChipState
import presentation.core.ui.source.kit.molecule.status.FeedbackBar
import presentation.core.ui.source.kit.molecule.status.FeedbackStatus
import presentation.core.ui.source.kit.molecule.status.StatusLine
import presentation.core.ui.source.kit.molecule.status.StatusStep
import presentation.core.ui.source.kit.molecule.status.StatusStepState
import presentation.core.ui.source.kit.atom.pill.StatusPillTone
import presentation.core.ui.source.kit.molecule.row.AnnotationRow
import presentation.core.ui.source.kit.molecule.row.HierarchyRow
import presentation.core.ui.source.kit.molecule.row.HistoryRow
import presentation.core.ui.source.kit.molecule.row.KeyValueRow
import presentation.core.ui.source.kit.molecule.row.KeyValueState
import presentation.core.ui.source.kit.molecule.row.SourceListHeader
import presentation.core.ui.source.kit.molecule.row.SourceListRow
import presentation.core.ui.source.kit.molecule.row.VerificationOutcome
import presentation.core.ui.source.kit.molecule.row.VerificationRow

/**
 * Living visual QA screen for the design system — every token and component rendered by the
 * real kit against the real theme. This is the canonical reference; keep it in sync as the
 * kit evolves rather than maintaining a separate static doc.
 *
 * @param onBack Invoked when the user taps the back affordance in the header.
 */
@Composable
public fun StyleguideScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.color.bg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            bottom = Theme.spacing.spacing3XL,
        ),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
    ) {

        // ── Header ──────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Theme.color.surface)
                    .padding(Theme.spacing.spacingL),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(Theme.cornerToken.chip)
                        .background(Theme.color.stage)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Back",
                            onClick = onBack,
                        )
                        .padding(Theme.spacing.spacingS),
                ) {
                    TechnicalLabel(text = "← BACK")
                }
                Spacer(Modifier.width(Theme.spacing.spacingS))
                Text(
                    text = "RECTIFY",
                    style = Theme.typography.titleL,
                    color = Theme.color.ink,
                )
                Spacer(Modifier.weight(1f))
                TechnicalLabel(text = "UI KIT V2")
            }
        }

        // ── 01 / Colour ─────────────────────────────────────────────────────
        item { SectionHeader(title = "01 / COLOUR") }
        item {
            ShowcasePanel {
                TechnicalLabel(text = "SURFACES")
                ColorRow(label = "bg", color = Theme.color.bg, outlined = true)
                ColorRow(label = "chrome", color = Theme.color.chrome, outlined = true)
                ColorRow(label = "stage", color = Theme.color.stage, outlined = true)
                ColorRow(label = "surface", color = Theme.color.surface, outlined = true)
                ColorRow(label = "pop", color = Theme.color.pop)
                ShowDivider()
                TechnicalLabel(text = "INK")
                ColorRow(label = "ink", color = Theme.color.ink)
                ColorRow(label = "ink2", color = Theme.color.ink2)
                ColorRow(label = "ink3", color = Theme.color.ink3)
                ColorRow(label = "primary", color = Theme.color.primary)
                ColorRow(label = "primaryHover", color = Theme.color.primaryHover)
                ColorRow(label = "primaryPress", color = Theme.color.primaryPress)
                ColorRow(label = "onPrimary — text on primary fill", color = Theme.color.onPrimary, outlined = true)
                ColorRow(label = "focus", color = Theme.color.focus)
                ShowDivider()
                TechnicalLabel(text = "LINES & FILLS")
                ColorRow(label = "line", color = Theme.color.line, outlined = true)
                ColorRow(label = "line2", color = Theme.color.line2, outlined = true)
                ColorRow(label = "hover", color = Theme.color.hover, outlined = true)
                ColorRow(label = "seg", color = Theme.color.seg, outlined = true)
                ColorRow(label = "codeBg", color = Theme.color.codeBg, outlined = true)
                ShowDivider()
                TechnicalLabel(text = "SIGNAL — SAME IN BOTH THEMES")
                ColorRow(label = "annotate", color = Theme.color.annotate)
                ColorRow(label = "sentFixed", color = Theme.color.sentFixed)
                ColorRow(label = "warnStillOff", color = Theme.color.warnStillOff)
                ColorRow(label = "measure", color = Theme.color.measure)
            }
        }

        // ── 02 / Typography ─────────────────────────────────────────────────
        item { SectionHeader(title = "02 / TYPOGRAPHY") }
        item {
            ShowcasePanel {
                TypographyRow("TITLE-L · ROBOTO 600 · 20", "Choose what to mirror", Theme.typography.titleL)
                TypographyRow("TITLE-M · ROBOTO 600 · 18", "Waiting for an Android device", Theme.typography.titleM)
                TypographyRow("TITLE-S · ROBOTO 600 · 15", "adb isn't on your PATH", Theme.typography.titleS)
                TypographyRow("BODY · ROBOTO 400 · 13", "Mirroring starts on its own as soon as adb sees one.", Theme.typography.body)
                TypographyRow("BODY-STRONG · ROBOTO 500 · 13", "Resolve & Send", Theme.typography.bodyStrong)
                TypographyRow("COMMENT · ROBOTO 400 · 12.5", "Price sits 6px below the title — align baselines", Theme.typography.comment)
                TypographyRow("LABEL · ROBOTO 600 · 12.5", "Verification", Theme.typography.label)
                TypographyRow("SECONDARY · ROBOTO 400 · 12", "Android 15 · USB", Theme.typography.secondary)
                TypographyRow("SMALL · ROBOTO 500 · 11.5", "Edit", Theme.typography.small)
                TypographyRow("CAPTION · ROBOTO 500 · 11", "Physical device", Theme.typography.caption)
                TypographyRow("MONO-DATA · JETBRAINS MONO 400 · 11", "TextView#item_price · 218×77", Theme.typography.monoData)
                TypographyRow("MONO-META · JETBRAINS MONO 400 · 10.5", "scrcpy 2.4 · 1344×2992 · 58 fps", Theme.typography.monoMeta)
                TypographyRow("OVERLINE · JETBRAINS MONO 400 · 10.5", "PAYLOAD", Theme.typography.overline)
                TypographyRow("MONO-BADGE · JETBRAINS MONO 500 · 10", "LIVE · 58 FPS", Theme.typography.monoBadge)
                TypographyRow("MONO-TAG · JETBRAINS MONO 500 · 9.5", "Δ6dp", Theme.typography.monoTag)
                TypographyRow("MONO-RULER · JETBRAINS MONO 400 · 8.5", "1000", Theme.typography.monoRuler)
                ShowDivider()
                TechnicalLabel(text = "LEGACY — NOT PART OF THE UI KIT V2 SCALE, BENTOTILE ONLY")
                TypographyRow("DISPLAY · ROBOTO 800 · 52", "128", Theme.typography.display)
                TypographyRow("NUMERAL XL · ROBOTO 700 · 34", "3 240", Theme.typography.numeralXl)
                TypographyRow("NUMERAL LG · ROBOTO 700 · 26", "47.2", Theme.typography.numeralLg)
            }
        }

        // ── 03 / Bento grid ─────────────────────────────────────────────────
        item { SectionHeader(title = "03 / BENTO GRID") }
        item {
            ShowcasePanel {
                TechnicalLabel(text = "ONE TILE, ONE THOUGHT · ONE HERO PER SCREEN · 40 MS CASCADE")
                BentoTile(
                    modifier = Modifier.fillMaxWidth().cascadeIn(0),
                    caption = "REVENUE",
                    value = "128",
                    unit = "K",
                    variant = BentoTileVariant.Hero,
                    height = BentoTileHeight.M,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                    BentoTile(
                        modifier = Modifier.weight(1f).cascadeIn(1),
                        caption = "USERS",
                        value = "3 240",
                        unit = "ACTIVE",
                    )
                    BentoTile(
                        modifier = Modifier.weight(1f).cascadeIn(2),
                        caption = "UPTIME",
                        value = "82",
                        unit = "/ 100",
                        variant = BentoTileVariant.Cool,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                    BentoTile(
                        modifier = Modifier.weight(1f).cascadeIn(3),
                        caption = "LATENCY",
                        value = "47.2",
                        unit = "MS",
                        variant = BentoTileVariant.Bone,
                    )
                    BentoTile(
                        modifier = Modifier.weight(1f).cascadeIn(4),
                        caption = "STORAGE SAVED",
                        value = "1.8",
                        unit = "GB",
                        variant = BentoTileVariant.Outline,
                    )
                }
                ShowDivider()
                TechnicalLabel(text = "HEIGHTS — NO ARBITRARY VALUES")
                TechnicalLabel(text = "S 104 · M 140 · L 196 · XL 240")
            }
        }

        // ── 04 / Radius ─────────────────────────────────────────────────────
        item { SectionHeader(title = "04 / RADIUS") }
        item {
            ShowcasePanel {
                TechnicalLabel(text = "NO SHARP CORNERS — SOFTEN EVERY SURFACE THE USER TOUCHES")
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                    CornerBox(label = "XS 8", radius = Theme.radius.xs)
                    CornerBox(label = "SM 12", radius = Theme.radius.sm)
                    CornerBox(label = "MD 16", radius = Theme.radius.md)
                    CornerBox(label = "LG 22", radius = Theme.radius.lg)
                    CornerBox(label = "XL 28", radius = Theme.radius.xl)
                    CornerBox(label = "PILL", radius = Theme.radius.pill)
                }
            }
        }

        // ── 05 / Spacing & stroke ───────────────────────────────────────────
        item { SectionHeader(title = "05 / SPACING & STROKE") }
        item {
            ShowcasePanel {
                SpacingRow(label = "XS", value = Theme.spacing.spacingXS)
                SpacingRow(label = "S", value = Theme.spacing.spacingS)
                SpacingRow(label = "L", value = Theme.spacing.spacingL)
                SpacingRow(label = "XL", value = Theme.spacing.spacingXL)
                SpacingRow(label = "2XL", value = Theme.spacing.spacing2XL)
                SpacingRow(label = "3XL", value = Theme.spacing.spacing3XL)
                ShowDivider()
                StrokeRow(label = "HAIRLINE 0.5", width = Theme.stroke.hairline)
                StrokeRow(label = "THIN 1 — TILE & CONTROL OUTLINE", width = Theme.stroke.thin)
                StrokeRow(label = "REGULAR 2 — ACTIVE EMPHASIS", width = Theme.stroke.regular)
            }
        }

        // ── 04 / Buttons ─────────────────────────────────────────────────────
        item { SectionHeader(title = "06 / BUTTONS") }
        item {
            ShowcasePanel {
                TechnicalLabel(text = "STATES — PRIMARY / SECONDARY / GHOST")
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                    AppButton(text = "Send", onClick = {})
                    AppButton(text = "Boot", onClick = {}, variant = AppButtonVariant.Secondary)
                    AppButton(text = "Clear", onClick = {}, variant = AppButtonVariant.Ghost)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                    AppButton(text = "Send", onClick = {}, enabled = false)
                    AppButton(text = "Boot", onClick = {}, variant = AppButtonVariant.Secondary, enabled = false)
                    AppButton(text = "Send", onClick = {}, busy = true, busyText = "Sending…")
                }
                ShowDivider()
                TechnicalLabel(text = "SIZES — 22 · 24 · 26 · 28 · 30 · 32")
                AppButton(text = "Verify", onClick = {}, size = AppButtonSize.InlineRow)
                AppButton(text = "Boot", onClick = {}, size = AppButtonSize.ListAction)
                AppButton(text = "Reconnect", onClick = {}, size = AppButtonSize.Banner)
                AppButton(text = "Resolve & Send", onClick = {}, size = AppButtonSize.Toolbar)
                AppButton(text = "Locate adb…", onClick = {}, size = AppButtonSize.Dialog)
                AppButton(text = "Start mirroring", onClick = {}, size = AppButtonSize.PageCta)
                ShowDivider()
                TechnicalLabel(text = "COMPOSITIONS")
                AppButton(text = "Resolve & Send", onClick = {}, shortcut = "⌘↵")
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                    AppButton(text = "Locate adb…", onClick = {})
                    AppButton(text = "Retry", onClick = {}, variant = AppButtonVariant.Ghost)
                }
                AppButton(
                    text = "Send follow-up · 1",
                    onClick = {},
                    shortcut = "⌘↵",
                    fillMaxWidth = true,
                )
            }
        }

        // ── 07 / Toolbar controls ───────────────────────────────────────────
        item { SectionHeader(title = "07 / TOOLBAR CONTROLS") }
        item {
            var platformIndex by remember { mutableIntStateOf(0) }
            var sidebarIndex by remember { mutableIntStateOf(0) }
            ShowcasePanel {
                TechnicalLabel(text = "PLATFORM SWITCH")
                SegmentedButtonGroup(
                    items = listOf("Android", "iOS"),
                    selectedIndex = platformIndex,
                    onSelect = { platformIndex = it },
                )
                ShowDivider()
                TechnicalLabel(text = "DEVICE CHIP · READY / WARN / LOST / NONE")
                DeviceChip(name = "Pixel 8 Pro", state = DeviceChipState.Ready, detail = "USB", onClick = {})
                DeviceChip(name = "iPhone 15 Pro", state = DeviceChipState.Warn, detail = "Shutdown", onClick = {})
                DeviceChip(name = "Pixel 8 Pro", state = DeviceChipState.Lost, detail = "reconnecting", onClick = {})
                DeviceChip(name = "No device", state = DeviceChipState.None, onClick = {})
                ShowDivider()
                TechnicalLabel(text = "COUNTER · EMPTY / STAGED / SENT")
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL)) {
                    CounterBadge(count = 0, state = CounterBadgeState.Empty, label = "staged")
                    CounterBadge(count = 3, state = CounterBadgeState.Staged, label = "staged")
                    CounterBadge(count = 3, state = CounterBadgeState.Sent, label = "sent")
                }
                ShowDivider()
                TechnicalLabel(text = "SIDEBAR TABS")
                SidebarTabs(
                    items = listOf(
                        SidebarTabItem(label = "Annotations", count = 3),
                        SidebarTabItem(label = "Payload"),
                        SidebarTabItem(label = "History", count = 1),
                    ),
                    selectedIndex = sidebarIndex,
                    onSelect = { sidebarIndex = it },
                )
                ShowDivider()
                TechnicalLabel(text = "KEY CAPS")
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS)) {
                    KeyCap(text = "⌥")
                    KeyCap(text = "⌘")
                    KeyCap(text = "↵")
                    KeyCap(text = "esc")
                }
            }
        }

        // ── 08 / Annotation marks ───────────────────────────────────────────
        item { SectionHeader(title = "08 / ANNOTATION MARKS") }
        item {
            ShowcasePanel {
                TechnicalLabel(text = "OVER THE DEVICE FRAME · RED IS RESERVED FOR ANNOTATIONS")
                AnnotationFrame {
                    AnnotationMark(
                        modifier = Modifier.fillMaxWidth(0.72f).height(56.dp),
                        state = AnnotationState.Drawing,
                        sizeLabel = "545 × 254",
                    )
                }
                TechnicalLabel(text = "drawing · dashed + size")
                AnnotationFrame {
                    AnnotationMark(
                        modifier = Modifier.fillMaxWidth(0.72f).height(56.dp),
                        state = AnnotationState.Pinned,
                        badge = "2",
                    )
                }
                TechnicalLabel(text = "pinned")
                AnnotationFrame {
                    AnnotationMark(
                        modifier = Modifier.fillMaxWidth(0.72f).height(56.dp),
                        state = AnnotationState.Selected,
                        badge = "2",
                        showHandles = true,
                    )
                }
                TechnicalLabel(text = "selected · handles")
                // Frozen dims everything outside the mark — the scrim is the host's to draw.
                AnnotationFrame(scrimAlpha = 0.42f) {
                    AnnotationMark(
                        modifier = Modifier.fillMaxWidth(0.72f).height(56.dp),
                        state = AnnotationState.Frozen,
                    )
                }
                TechnicalLabel(text = "frozen · outside dims 42%")
                ShowDivider()
                AnnotationFrame {
                    AnnotationMark(
                        modifier = Modifier.fillMaxWidth(0.72f).height(56.dp),
                        state = AnnotationState.Sent,
                        badge = "✓",
                    )
                }
                TechnicalLabel(text = "sent / verified fixed")
                AnnotationFrame {
                    AnnotationMark(
                        modifier = Modifier.fillMaxWidth(0.72f).height(56.dp),
                        state = AnnotationState.StillOff,
                        badge = "!",
                    )
                }
                TechnicalLabel(text = "verified · still off")
                ShowDivider()
                ShowDivider()
                TechnicalLabel(text = "OVERLAY · SCRIM PUNCHED OUT AROUND THE MARKS")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(OverlayFrameHeight)
                        .clip(Theme.cornerToken.card)
                        .background(Theme.color.surface),
                ) {
                    AnnotationOverlay(marks = StyleguideOverlayMarks, dimmed = true)
                }
                TechnicalLabel(text = "frozen · the marks stay readable, everything else dims 42%")
                ShowDivider()
                TechnicalLabel(text = "GUTTER POPOVER")
                AnnotationPopover(text = "Price sits 6px below the title", index = 1)
                ShowDivider()
                TechnicalLabel(text = "RULERS · DEVICE PX, EXTENT IN RED")
                Ruler(totalPx = 1344, extentPx = 272..800)
            }
        }

        // ── 09 / Comment popover ────────────────────────────────────────────
        item { SectionHeader(title = "09 / COMMENT POPOVER") }
        item {
            var newComment by remember { mutableStateOf("") }
            ShowcasePanel {
                TechnicalLabel(text = "ALWAYS DARK, IN BOTH THEMES, SO IT READS OVER ANY APP UI")
                CommentPopover(
                    value = newComment,
                    onValueChange = { newComment = it },
                    onClose = {},
                    index = 1,
                    meta = "218×77 @ 1035,862",
                    saveHint = "⏎ save",
                )
                TechnicalLabel(text = "new · focused (click the field)")
                CommentPopover(
                    value = "Stepper is 24dp tall; hit target too small",
                    onValueChange = {},
                    onClose = {},
                    index = 2,
                    meta = "218×77 @ 1035,862",
                )
                TechnicalLabel(text = "filled")
                CommentPopover(
                    value = "Price sits lower than the title",
                    onValueChange = {},
                    onClose = {},
                    index = 1,
                    meta = "218×77 @ 1035,862",
                    finding = "⌥ top Δ6dp vs item_title",
                )
                TechnicalLabel(text = "with measured finding")
            }
        }

        // ── 10 / Measurement ────────────────────────────────────────────────
        item { SectionHeader(title = "10 / MEASUREMENT") }
        item {
            ShowcasePanel {
                TechnicalLabel(text = "BLUE IS RESERVED FOR ⌥ MEASURE")
                // The connector lines that carry the gap labels are routed by the host, so the
                // overlay below places its marks and tags by hand rather than pretending the
                // kit knows the geometry.
                AnnotationFrame {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
                    ) {
                        MeasureMark(
                            modifier = Modifier.width(120.dp).height(32.dp),
                            role = MeasureMarkRole.Neighbour,
                        )
                        MeasureTag(text = "60", containerColor = Theme.color.measure)
                        MeasureMark(modifier = Modifier.width(96.dp).height(32.dp))
                    }
                }
                TechnicalLabel(text = "target solid · neighbours dashed · gap labels")
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                    MeasureTag(text = "Δ6dp")
                    MeasureTag(text = "50", containerColor = Theme.color.measure)
                }
                TechnicalLabel(text = "Δ misalignment is red · gap labels are blue")
                ShowDivider()
                TechnicalLabel(text = "MEASURE CARD")
                MeasureCard(
                    nodeId = "TextView#item_price",
                    index = 1,
                    specs = listOf(
                        MeasureSpec("Size", "48×17dp"),
                        MeasureSpec("Top", "9dp"),
                        MeasureSpec("Right", "20dp"),
                        MeasureSpec("Bottom", "49dp"),
                        MeasureSpec("Left", "62dp"),
                    ),
                    finding = "top Δ6dp vs item_title · expected 0",
                )
            }
        }

        // ── 11 / Status & feedback ──────────────────────────────────────────
        item { SectionHeader(title = "11 / STATUS & FEEDBACK") }
        item {
            ShowcasePanel {
                TechnicalLabel(text = "PIPELINE")
                StatusLine(
                    steps = listOf(
                        StatusStep("Streaming", StatusStepState.Live),
                        StatusStep("Frozen"),
                        StatusStep("Dumping hierarchy"),
                        StatusStep("Sent"),
                    ),
                    trailing = "live",
                )
                StatusLine(
                    steps = listOf(
                        StatusStep("Streaming"),
                        StatusStep("Frozen", StatusStepState.Frozen),
                        StatusStep("Dumping hierarchy"),
                        StatusStep("Sent"),
                    ),
                    trailing = "frozen",
                )
                StatusLine(
                    steps = listOf(
                        StatusStep("Streaming"),
                        StatusStep("Frozen"),
                        StatusStep("Dumping hierarchy", StatusStepState.Busy),
                        StatusStep("Sent"),
                    ),
                    trailing = "sending",
                )
                StatusLine(
                    steps = listOf(
                        StatusStep("Streaming"),
                        StatusStep("Frozen"),
                        StatusStep("Dumping hierarchy"),
                        StatusStep("Sent to Claude Code", StatusStepState.Done),
                    ),
                    trailing = "sent",
                )
                StatusLine(
                    steps = listOf(
                        StatusStep("Stream dropped", StatusStepState.Error),
                        StatusStep("Frozen", StatusStepState.Frozen),
                        StatusStep("Dumping hierarchy"),
                        StatusStep("Sent"),
                    ),
                    trailing = "failed",
                )
                ShowDivider()
                TechnicalLabel(text = "FEEDBACK")
                FeedbackBar(
                    text = "Sent 3 annotations to Claude Code",
                    timing = "1.4 s",
                    actionLabel = "Open session",
                )
                FeedbackBar(
                    text = "Stream dropped",
                    status = FeedbackStatus.Error,
                    detail = "Annotations kept.",
                    actionLabel = "Reconnect",
                    actionVariant = AppButtonVariant.Primary,
                )
                ShowDivider()
                TechnicalLabel(text = "STATUS LINE")
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL)) {
                    StatusIndicator(state = StatusIndicatorState.Live, label = "LIVE · 58 FPS")
                    StatusIndicator(state = StatusIndicatorState.Frozen, label = "FROZEN · FRAME 18422")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingL)) {
                    StatusIndicator(state = StatusIndicatorState.Dumping)
                    StatusIndicator(state = StatusIndicatorState.Sent)
                    StatusIndicator(state = StatusIndicatorState.Offline)
                }
            }
        }

        // ── 12 / Errors ─────────────────────────────────────────────────────
        item { SectionHeader(title = "12 / ERRORS") }
        item {
            ShowcasePanel {
                TechnicalLabel(text = "PROBLEM → ONE PRIMARY ACTION → EXACT COMMAND → DIAGNOSTIC")
                ErrorCard(
                    title = "adb isn't on your PATH",
                    body = "Install platform-tools, or point Rectify at an adb you already have.",
                    command = "brew install --cask android-platform-tools",
                    primaryActionLabel = "Locate adb…",
                    secondaryActionLabel = "Retry",
                    diagnostic = "which adb → not found · \$ANDROID_HOME unset",
                )
                ShowDivider()
                TechnicalLabel(text = "GLYPH · FIXABLE / CHOICE / FAILED")
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                    ErrorGlyph(kind = ErrorKind.Fixable)
                    ErrorGlyph(kind = ErrorKind.Choice)
                    ErrorGlyph(kind = ErrorKind.Failed)
                }
            }
        }

        // ── 13 / Lists & rows ───────────────────────────────────────────────
        item { SectionHeader(title = "13 / LISTS & ROWS") }
        item {
            var sourceIndex by remember { mutableIntStateOf(0) }
            ShowcasePanel {
                TechnicalLabel(text = "SOURCE LIST · SELECTED / RUNNING / SHUT DOWN")
                SourceListHeader(text = "Physical device")
                SourceListRow(
                    name = "Pixel 8 Pro",
                    serial = "37281FDH…",
                    selected = sourceIndex == 0,
                    onClick = { sourceIndex = 0 },
                ) {
                    Text(
                        text = "USB",
                        style = Theme.typography.monoMeta,
                        color = Theme.color.ink2,
                    )
                }
                SourceListRow(
                    name = "Pixel_7_API_34",
                    selected = sourceIndex == 1,
                    onClick = { sourceIndex = 1 },
                ) {
                    StatusIndicator(state = StatusIndicatorState.Sent, label = "RUNNING")
                }
                SourceListRow(
                    name = "Pixel_Fold_API_35",
                    selected = sourceIndex == 2,
                    onClick = { sourceIndex = 2 },
                ) {
                    AppButton(
                        text = "Boot",
                        onClick = {},
                        variant = AppButtonVariant.Secondary,
                        size = AppButtonSize.ListAction,
                    )
                }
                ShowDivider()
                TechnicalLabel(text = "HISTORY ROW · STATUS PILLS")
                HistoryRow(
                    title = "Price sits 6px below the title",
                    meta = "3 notes · 14:02",
                    statusLabel = "Fixed",
                    statusTone = StatusPillTone.Done,
                    onClick = {},
                ) {
                    AppButton(text = "Verify", onClick = {}, size = AppButtonSize.ListAction)
                }
                HistoryRow(
                    title = "Checkout button off the margin",
                    meta = "1 note · 13:47",
                    statusLabel = "In progress",
                    statusTone = StatusPillTone.Busy,
                    onClick = {},
                )
                HistoryRow(
                    title = "Promo placeholder clipped",
                    meta = "1 note · 12:10",
                    statusLabel = "Verified",
                    statusTone = StatusPillTone.Neutral,
                    onClick = {},
                )
                HistoryRow(
                    title = "Total row reads lighter",
                    meta = "2 notes · yesterday",
                    statusLabel = "Follow-up sent",
                    statusTone = StatusPillTone.Alert,
                    onClick = {},
                )
                ShowDivider()
                TechnicalLabel(text = "ANNOTATION ROW · SELECTED / EMPTY")
                AnnotationRow(
                    index = 1,
                    comment = "Price sits 6px below the title — align baselines",
                    meta = "218×77 @ 1035,862",
                    finding = "⌥ top Δ6dp vs item_title · expected 0",
                    selected = true,
                    onClick = {},
                )
                AnnotationRow(index = 2, meta = "436×154 @ 354,1003", onClick = {})
                ShowDivider()
                TechnicalLabel(text = "VERIFICATION ROW · FIXED / STILL OFF")
                VerificationRow(
                    title = "Price sits 6px below the title",
                    delta = "top Δ6dp → 0dp",
                    outcome = VerificationOutcome.Fixed,
                )
                VerificationRow(
                    title = "Stepper hit target",
                    delta = "height 24dp → 32dp · want 48dp",
                    outcome = VerificationOutcome.StillOff,
                )
                ShowDivider()
                TechnicalLabel(text = "KEY / VALUE · NEUTRAL / BUSY / DONE")
                KeyValueRow(label = "Frame", value = "1344×2992 PNG")
                KeyValueRow(label = "Hierarchy", value = "dumping…", state = KeyValueState.Busy)
                KeyValueRow(label = "Hierarchy", value = "412 nodes", state = KeyValueState.Done)
                ShowDivider()
                TechnicalLabel(text = "HIERARCHY · MATCHED NODES TAGGED")
                // The panel spaces its children apart, which would cut the tree guides into
                // floating dashes — a tree has to be stacked with no gaps.
                Column(verticalArrangement = Arrangement.Top) {
                    HierarchyRow(nodeId = "CartItemView[1]", depth = 0)
                    HierarchyRow(nodeId = "TextView#item_price", depth = 1, tag = "#1", matched = true)
                    HierarchyRow(nodeId = "QtyStepper#qty", depth = 1, last = true, tag = "#2", matched = true)
                }
            }
        }

        // ── 14 / Sliders, bars & progress ───────────────────────────────────
        item { SectionHeader(title = "14 / SLIDERS, BARS & PROGRESS") }
        item {
            var brightness by remember { mutableFloatStateOf(0.72f) }
            var volume by remember { mutableFloatStateOf(0.34f) }
            ShowcasePanel {
                TechnicalLabel(text = "BRIGHTNESS · ${(brightness * 100).toInt()}%")
                WavySlider(value = brightness, onValueChange = { brightness = it })
                TechnicalLabel(text = "VOLUME · ${(volume * 100).toInt()}%")
                WavySlider(value = volume, onValueChange = { volume = it })
                ShowDivider()
                TechnicalLabel(text = "UPLOAD · 34%")
                SegmentedProgressIndicator(progress = 0.34f)
                TechnicalLabel(text = "OVER LIMIT · 78%")
                SegmentedProgressIndicator(progress = 0.78f, activeColor = Theme.color.warnStillOff)
                ShowDivider()
                TechnicalLabel(text = "7 DAY TREND")
                SegmentedBars(
                    values = listOf(34f, 52f, 70f, 88f, 100f, 74f, 58f, 42f, 66f, 80f),
                    highlightIndex = 4,
                )
            }
        }

        // ── 15 / Selection controls ─────────────────────────────────────────
        item { SectionHeader(title = "15 / SELECTION CONTROLS") }
        item {
            var toggleOn by remember { mutableStateOf(true) }
            var segmentIndex by remember { mutableIntStateOf(0) }
            var chipIndex by remember { mutableIntStateOf(1) }
            ShowcasePanel {
                ToggleRow(label = "REDUCED MOTION", checked = toggleOn, onCheckedChange = { toggleOn = it })
                ToggleRow(label = "DISABLED", checked = false, onCheckedChange = {}, enabled = false)
                ShowDivider()
                SegmentedButtonGroup(
                    items = listOf("TODAY", "WEEK", "MONTH"),
                    selectedIndex = segmentIndex,
                    onSelect = { segmentIndex = it },
                )
                ShowDivider()
                Row(horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS)) {
                    listOf("All", "Starred", "Recent").forEachIndexed { index, label ->
                        Chip(
                            label = label,
                            selected = index == chipIndex,
                            onClick = { chipIndex = index },
                        )
                    }
                }
            }
        }

        // ── 16 / Input ─────────────────────────────────────────────
        item { SectionHeader(title = "16 / INPUT") }
        item {
            ShowcasePanel {
                Input(
                    onTextChanged = {},
                    placeholder = "DEVICE NAME",
                )
            }
        }

        // ── 17 / Tab bar ────────────────────────────────────────────────────
        item { SectionHeader(title = "17 / TAB BAR") }
        item {
            var tabIndex by remember { mutableIntStateOf(0) }
            ShowcasePanel {
                TabBar(
                    items = listOf(
                        TabBarItem(icon = Home, contentDescription = "Home"),
                        TabBarItem(icon = Wifi, contentDescription = "Activity"),
                        TabBarItem(icon = RefreshCcw, contentDescription = "History"),
                        TabBarItem(icon = Settings, contentDescription = "Settings"),
                    ),
                    selectedIndex = tabIndex,
                    onItemClick = { tabIndex = it },
                )
            }
        }

        // ── 18 / Motion ─────────────────────────────────────────────────────
        item { SectionHeader(title = "18 / MOTION") }
        item {
            ShowcasePanel {
                MotionRow("INSTANT", "${Theme.motion.durations.instantMs} MS", "press, tab switch")
                MotionRow("FAST", "${Theme.motion.durations.fastMs} MS", "pills, toggles, states")
                MotionRow("NORMAL", "${Theme.motion.durations.normalMs} MS", "tile entrance, expansion")
                MotionRow("SLOW", "${Theme.motion.durations.slowMs} MS", "screen transition, sheet")
                MotionRow("CASCADE", "${Theme.motion.durations.tileCascadeMs} MS", "per-tile stagger")
                MotionRow("NUMERAL", "${Theme.motion.durations.numeralRefreshMs} MS", "live value throttle ≈ 4 Hz")
                ShowDivider()
                TechnicalLabel(
                    text = if (Theme.motion.reducedMotion) {
                        "REDUCED MOTION ON — DURATIONS ABOVE ARE ALREADY SUPPRESSED"
                    } else {
                        "REDUCED MOTION OFF — SET IT ON AppTheme, NOT AT CALL-SITES"
                    },
                )
            }
        }

        // ── 19 / Do & Don't ─────────────────────────────────────────────────
        item { SectionHeader(title = "19 / DO & DON'T") }
        item {
            ShowcasePanel {
                RuleRow(positive = true, text = "One tile, one thought")
                RuleRow(positive = true, text = "Mono micro-caption above a big number")
                RuleRow(positive = true, text = "One hero tile per screen, always first")
                RuleRow(positive = true, text = "JetBrains Mono for ruler numerals only")
                RuleRow(positive = true, text = "Status colours in the data layer only")
                ShowDivider()
                RuleRow(positive = false, text = "More than two accent tiles on a screen")
                RuleRow(positive = false, text = "Shadows, blur, glassmorphism")
                RuleRow(positive = false, text = "Gradients on tiles")
                RuleRow(positive = false, text = "Tile heights outside the scale")
                RuleRow(positive = false, text = "Proportional figures for values")
                RuleRow(positive = false, text = "Elastic, bounce, shimmer")
                RuleRow(positive = false, text = "Hardcoded Color(0x…) in the kit")
                RuleRow(positive = false, text = "Status colours on buttons and tabs")
            }
        }
    }
}

// ── Private helpers ──────────────────────────────────────────────────────────

/** Section container — every showcase block sits on one raised surface. */
@Composable
private fun ShowcasePanel(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Theme.spacing.spacingS)
            .clip(Theme.cornerToken.tile)
            .background(Theme.color.surface)
            .padding(Theme.spacing.spacingL),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
        content = content,
    )
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Theme.color.bg)
            .padding(
                horizontal = Theme.spacing.spacingL,
                vertical = Theme.spacing.spacingS,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Box(
            modifier = Modifier
                .width(Theme.stroke.regular)
                .height(Theme.spacing.spacingL)
                .clip(Theme.cornerToken.chip)
                .background(Theme.color.primary),
        )
        TechnicalLabel(text = title)
    }
}

@Composable
private fun ColorRow(label: String, color: Color, outlined: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        val shape = Theme.cornerToken.badge
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 24.dp)
                .clip(shape)
                .background(color)
                .let { m ->
                    if (outlined) m.border(Theme.stroke.thin, Theme.color.line2, shape) else m
                },
        )
        TechnicalLabel(text = label, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TypographyRow(role: String, sample: String, style: TextStyle) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TechnicalLabel(text = role)
        Spacer(Modifier.height(Theme.spacing.spacingXS))
        Text(
            text = sample,
            style = style,
            color = Theme.color.ink,
            maxLines = 1,
        )
    }
}

@Composable
private fun SpacingRow(label: String, value: Dp) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        TechnicalLabel(text = label, modifier = Modifier.width(Theme.spacing.spacing2XL))
        TechnicalLabel(
            text = "${value.value.toInt()}dp",
            modifier = Modifier.width(Theme.spacing.spacing2XL),
        )
        Box(
            modifier = Modifier
                .height(Theme.stroke.regular)
                .width(value)
                .background(Theme.color.primary),
        )
    }
}

@Composable
private fun CornerBox(label: String, radius: Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
    ) {
        Box(
            modifier = Modifier
                .size(Theme.spacing.spacing3XL)
                .border(
                    width = Theme.stroke.regular,
                    color = Theme.color.primary,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        radius.coerceAtMost(Theme.spacing.spacingXL),
                    ),
                ),
        )
        TechnicalLabel(text = label, textAlign = TextAlign.Center)
    }
}

@Composable
private fun StrokeRow(label: String, width: Dp) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.spacingXS),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(width)
                .background(Theme.color.primary),
        )
        TechnicalLabel(text = label)
    }
}

@Composable
private fun MotionRow(label: String, value: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        TechnicalLabel(text = label, modifier = Modifier.width(Theme.spacing.spacing4XL))
        Text(
            text = value,
            style = Theme.typography.monoData,
            color = Theme.color.primary,
            modifier = Modifier.width(Theme.spacing.spacing4XL),
        )
        TechnicalLabel(text = description, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RuleRow(positive: Boolean, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        Text(
            text = if (positive) "+" else "−",
            style = Theme.typography.monoData,
            color = if (positive) Theme.color.sentFixed else Theme.color.annotate,
            modifier = Modifier.width(Theme.spacing.spacingL),
        )
        TechnicalLabel(text = text, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.spacingS),
    ) {
        SimpleToggle(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
        TechnicalLabel(text = label)
    }
}

/** Showcase-only overlay geometry. */
private val OverlayFrameHeight: Dp = 150.dp

/** Showcase-only marks: a labelled pinned mark and a selected one with handles. */
private val StyleguideOverlayMarks = listOf(
    AnnotationOverlayMark(
        bounds = DpRect(left = 32.dp, top = 24.dp, right = 190.dp, bottom = 74.dp),
        badge = "1",
        sizeLabel = "545 × 254",
    ),
    AnnotationOverlayMark(
        bounds = DpRect(left = 150.dp, top = 88.dp, right = 300.dp, bottom = 132.dp),
        state = AnnotationState.Selected,
        badge = "2",
        showHandles = true,
    ),
)

/**
 * Stands in for the mirrored device frame an annotation mark normally sits on, so the marks in
 * this section are shown in the only context they are valid in.
 *
 * @param scrimAlpha Dim applied over the frame, used to demonstrate the frozen state.
 */
@Composable
private fun AnnotationFrame(
    scrimAlpha: Float = 0f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(Theme.cornerToken.card)
            .background(Theme.color.surface),
        contentAlignment = Alignment.Center,
    ) {
        if (scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Theme.color.ink.copy(alpha = scrimAlpha)),
            )
        }
        content()
    }
}

@Composable
private fun ShowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Theme.stroke.hairline)
            .background(Theme.color.line),
    )
}

@Composable
private fun TechnicalLabel(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        style = Theme.typography.overline,
        color = Theme.color.ink2,
        modifier = modifier,
        textAlign = textAlign,
    )
}
