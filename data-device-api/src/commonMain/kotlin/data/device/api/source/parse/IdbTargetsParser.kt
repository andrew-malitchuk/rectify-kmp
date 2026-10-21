package data.device.api.source.parse

import data.device.api.source.model.IosTargetResource

/** `idb list-targets` is pipe-delimited: name | udid | state | type | runtime | arch | companion. */
private const val COLUMN_SEPARATOR = '|'

/** The four columns this parser reads; a shorter row is not a target row. */
private const val REQUIRED_COLUMNS = 4

/** `targetType` of a physical handset, as opposed to `simulator`. */
private const val PHYSICAL_TARGET = "device"

/** The one state a physical handset reports when it is usable. */
private const val USABLE_PHYSICAL_STATE = "booted"

/**
 * THE definition of "this handset has not been trusted yet" (AS-2).
 *
 * One named set so the classification is correctable in one place when a real untrusted
 * device is finally measured — none was available on the capture host, which is why
 * [isTrusted] also carries a fallback rather than relying on this list being complete.
 */
private val UNTRUSTED_STATE_TOKENS = setOf(
    "unauthorized",
    "untrusted",
    "unpaired",
    "not paired",
    "pairing",
    "locked",
)

/**
 * Parses the stdout of `idb list-targets`.
 *
 * Never throws: a row without the four columns it needs is skipped. Both simulators and
 * physical handsets are returned — the caller selects with [isPhysical] — because `idb`
 * lists both and silently dropping half of its answer would hide a device the user is
 * holding.
 *
 * The captured fixture was taken with NO `idb_companion` running: every row read
 * `No Companion Connected`, the command still exited 0 and still listed every target. A
 * missing companion is therefore not an error path this parser has to model.
 *
 * @param output Raw stdout, one target per line.
 * @return One [IosTargetResource] per parsable row, in the order the tool listed them.
 */
public fun parseIdbTargets(output: String): List<IosTargetResource> = output.lineSequence()
    .map(String::trim)
    .filter { it.isNotEmpty() }
    .mapNotNull(::parseTargetRow)
    .toList()

/**
 * THE physical-handset predicate, and the only correct way to read a [parseIdbTargets] row.
 *
 * `idb list-targets` lists simulators beside handsets, and [IosTargetResource.trusted] is
 * `true` for every simulator by construction — so a caller that skips this filter counts a
 * shut-down simulator as a connected phone, offers it as a mirror target and can never reach
 * the boot-a-simulator state (AC-31). Public because the row is parsed here and classified by
 * the repository, which is a module away.
 */
public val IosTargetResource.isPhysical: Boolean
    get() = targetType.equals(PHYSICAL_TARGET, ignoreCase = true)

private fun parseTargetRow(line: String): IosTargetResource? {
    val columns = line.split(COLUMN_SEPARATOR).map(String::trim)
    if (columns.size < REQUIRED_COLUMNS) return null
    val (name, udid, state, targetType) = columns
    if (name.isEmpty() || udid.isEmpty()) return null
    return IosTargetResource(
        name = name,
        udid = udid,
        state = state,
        targetType = targetType,
        trusted = isTrusted(state = state, targetType = targetType),
    )
}

/**
 * Simulators are always drivable; a handset has to have been trusted.
 *
 * The fallback matters more than the token list: an unrecognised state on a PHYSICAL target
 * means "listed but not usable", and calling that trusted would offer the user a mirror
 * button that cannot work. Guessing untrusted costs one wrong retry prompt; guessing trusted
 * costs a dead stage.
 */
private fun isTrusted(state: String, targetType: String): Boolean = when {
    !targetType.equals(PHYSICAL_TARGET, ignoreCase = true) -> true
    UNTRUSTED_STATE_TOKENS.any { state.contains(it, ignoreCase = true) } -> false
    else -> state.equals(USABLE_PHYSICAL_STATE, ignoreCase = true)
}
