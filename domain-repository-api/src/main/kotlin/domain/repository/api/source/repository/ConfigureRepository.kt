package domain.repository.api.source.repository

import domain.core.source.model.ThemeModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing various system and UI configurations.
 */
public interface ConfigureRepository {
    /** Updates the application [theme]. */
    public suspend fun setTheme(theme: ThemeModel?)

    /** Observes changes to the application theme. */
    public fun observeTheme(): Flow<ThemeModel?>

    /** Observes changes to the application language locale code. */
    public fun observeApplicationLanguage(): Flow<String?>

    /** Retrieves the persisted shell side panel width, in dp. */
    public suspend fun getSidePanelWidth(): Int

    /** Updates the persisted shell side panel width to [widthDp], in dp. */
    public suspend fun setSidePanelWidth(widthDp: Int)
}
