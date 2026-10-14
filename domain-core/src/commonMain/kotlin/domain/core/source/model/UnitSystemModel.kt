package domain.core.source.model

import domain.core.source.model.base.Model

/**
 * Domain model representing the user's preferred measurement unit system.
 *
 * @property id Stable string identifier used for persistence (e.g. "metric").
 */
public enum class UnitSystemModel(public val id: String) : Model {
    /** SI units — km, °C. */
    Metric("metric"),

    /** Imperial units — mi, °F. */
    Imperial("imperial"),
}
