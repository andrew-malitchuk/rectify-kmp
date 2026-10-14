package presentation.core.styling.core

/**
 * Duration tokens (milliseconds) for Rectify animations.
 *
 * A four-step generic scale: motion is a property of the interaction, not of the screen it
 * happens on.
 *
 * Every value here is already **0 when [ThemeMotion.reducedMotion] is on** — the palette is
 * resolved once in `attributeMotion`, so call-sites never branch on reduced-motion themselves.
 *
 * @param instantMs Press feedback, tab switches — no animation (0 ms).
 * @param fastMs Pills, toggles, state changes (140 ms, ease-out).
 * @param normalMs Tile entrance, expansion (240 ms, ease-in-out).
 * @param slowMs Screen transition, bottom sheet (380 ms, ease-in-out).
 * @param tileCascadeMs Per-tile stagger when a bento grid appears (40 ms). The only
 *   decorative motion the system keeps.
 * @param numeralRefreshMs Minimum interval between live numeral updates (250 ms ≈ 4 Hz).
 *   Values step rather than interpolate, otherwise digits smear and stop being readable.
 *   Not zeroed when reduced motion is on — it is a throttle, not an animation.
 * @see ThemeMotion
 * @see Theme.motion
 */
public data class ThemeMotionDurations(
    val instantMs: Int = 0,
    val fastMs: Int = 140,
    val normalMs: Int = 240,
    val slowMs: Int = 380,
    val tileCascadeMs: Int = 40,
    val numeralRefreshMs: Int = 250,
)
