package data.device.api.source.parse

import data.device.api.source.model.SimulatorResource

/** Section headers are wrapped in these, e.g. `-- iOS 18.5 --`. */
private const val SECTION_MARKER = "--"

/** A section whose runtime is not installed; its rows are dropped, header and all. */
private const val UNAVAILABLE_SECTION = "Unavailable"

/** Per-row state for a simulator whose runtime went away, e.g. `(unavailable, runtime …)`. */
private const val UNAVAILABLE_STATE = "unavailable"

/**
 * Parses the stdout of `xcrun simctl list devices available`.
 *
 * Hand-rolled line scanning, the same shape as [parseAdbDevices] and for the same reason:
 * `data-device-api` has no serialization plugin and this module's one dependency is
 * `:data-core`.
 *
 * Never throws. A line that does not look like a device row is skipped, and an
 * `(unavailable, …)` row is dropped even though `available` should already have filtered it.
 *
 * The row shape is `    <name> (<udid>) (<state>) ` and **names contain parentheses**
 * (`iPhone SE (3rd generation)`), so the udid is the SECOND-TO-LAST parenthesised group, never
 * the first. Every captured row also carries a trailing space after the state.
 *
 * @param output Raw stdout, `== Devices ==` banner and runtime headers included.
 * @return One [SimulatorResource] per parsable row, in the order the tool listed them.
 */
public fun parseSimctlDevices(output: String): List<SimulatorResource> {
    var runtime: String? = null
    return output.lineSequence()
        .map(String::trim)
        .filter { it.isNotEmpty() }
        .mapNotNull { line ->
            when {
                line.isSectionHeader() -> {
                    runtime = line.sectionName()?.takeUnless { it.startsWith(UNAVAILABLE_SECTION) }
                    null
                }
                // A row before any header has no runtime to attribute it to; `== Devices ==`
                // and anything under an unavailable section land here too.
                else -> runtime?.let { line.parseSimulatorRow(it) }
            }
        }
        .toList()
}

private fun String.isSectionHeader(): Boolean = startsWith(SECTION_MARKER) && endsWith(SECTION_MARKER)

private fun String.sectionName(): String? = removeSurrounding(SECTION_MARKER).trim().takeIf { it.isNotEmpty() }

private fun String.parseSimulatorRow(runtime: String): SimulatorResource? {
    val (head, state) = splitTrailingGroup() ?: return null
    if (state.startsWith(UNAVAILABLE_STATE, ignoreCase = true)) return null
    val (name, udid) = head.splitTrailingGroup() ?: return null
    if (name.isEmpty() || udid.isEmpty()) return null
    return SimulatorResource(name = name, udid = udid, runtime = runtime, state = state)
}

/** Splits `"<head> (<group>)"` into its head and the LAST parenthesised group. */
private fun String.splitTrailingGroup(): Pair<String, String>? {
    if (!endsWith(')')) return null
    val open = lastIndexOf('(')
    if (open <= 0) return null
    return substring(0, open).trim() to substring(open + 1, length - 1)
}
