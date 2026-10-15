package data.device.api.source.model

import data.core.source.resource.Resource

/**
 * Result of probing for one tool. A missing tool is a value here, never an exception.
 *
 * @property kind Tool that was probed.
 * @property found `true` when an executable was located.
 * @property path Absolute path of the executable, when [found].
 * @property version Version string the tool reported, when [found].
 * @property probedPaths Every candidate tried, in probe order — rendered in the UI footer.
 * @property installCommand Exact shell line that installs the tool.
 * @property envHint Environment variable hint shown with [installCommand].
 */
public data class ToolProbe(
    val kind: ToolKind,
    val found: Boolean,
    val path: String? = null,
    val version: String? = null,
    val probedPaths: List<String> = emptyList(),
    val installCommand: String = "",
    val envHint: String = "",
) : Resource
