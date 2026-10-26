package domain.core.source.model.device

import domain.core.source.model.base.Model

/**
 * Result of probing for one external platform tool.
 *
 * A missing tool is a normal value here, never an error.
 *
 * @property platform Platform the tool serves.
 * @property isInstalled `true` when an executable was found.
 * @property version Version string reported by the tool, when found.
 * @property path Absolute path of the executable, when found.
 * @property probedPaths Every candidate path tried, in probe order.
 * @property installCommand Exact shell line the user can run to install the tool.
 */
public data class ToolModel(
    val platform: PlatformModel,
    val isInstalled: Boolean,
    val version: String? = null,
    val path: String? = null,
    val probedPaths: List<String> = emptyList(),
    val installCommand: String = "",
) : Model
