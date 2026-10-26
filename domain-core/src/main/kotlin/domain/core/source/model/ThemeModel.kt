package domain.core.source.model

import domain.core.source.model.base.Model

/**
 * Domain model representing the application's visual theme mode.
 *
 * @property mode The string identifier for the theme mode.
 */
public enum class ThemeModel(public val mode: String) : Model {
    /** Classic light mode. */
    Light("light"),

    /** Classic dark mode. */
    Dark("dark"),

    /**
     * Follow the platform's system appearance (light or dark).
     *
     * The stored value stays `"materialu"` for backwards compatibility with
     * installs that persisted it under the previous name — never rename it.
     */
    System("materialu"),
}
