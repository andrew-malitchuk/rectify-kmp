package presentation.core.ui.source.icon

import androidx.compose.ui.graphics.vector.ImageVector
import presentation.core.ui.source.kit.atom.icon.AlertTriangle
import presentation.core.ui.source.kit.atom.icon.Battery
import presentation.core.ui.source.kit.atom.icon.BookOpen
import presentation.core.ui.source.kit.atom.icon.Clock
import presentation.core.ui.source.kit.atom.icon.FileText
import presentation.core.ui.source.kit.atom.icon.Folder
import presentation.core.ui.source.kit.atom.icon.HardDrive
import presentation.core.ui.source.kit.atom.icon.Home
import presentation.core.ui.source.kit.atom.icon.Image
import presentation.core.ui.source.kit.atom.icon.Lock
import presentation.core.ui.source.kit.atom.icon.Plus
import presentation.core.ui.source.kit.atom.icon.RefreshCcw
import presentation.core.ui.source.kit.atom.icon.Settings
import presentation.core.ui.source.kit.atom.icon.Smartphone
import presentation.core.ui.source.kit.atom.icon.Sun
import presentation.core.ui.source.kit.atom.icon.Type
import presentation.core.ui.source.kit.atom.icon.Wifi

/** Ordered list of (key → ImageVector) pairs for the user-facing icon picker. */
public val iconCatalogue: List<Pair<String, ImageVector>> = listOf(
    "hard_drive" to HardDrive,
    "settings" to Settings,
    "wifi" to Wifi,
    "smartphone" to Smartphone,
    "refresh" to RefreshCcw,
    "clock" to Clock,
    "file_text" to FileText,
    "home" to Home,
    "lock" to Lock,
    "book" to BookOpen,
    "image" to Image,
    "type" to Type,
    "sun" to Sun,
    "alert" to AlertTriangle,
    "battery" to Battery,
    "folder" to Folder,
    "plus" to Plus,
)

/**
 * Resolves a stored icon key to an [ImageVector].
 * Returns [HardDrive] when [key] is blank or unrecognised.
 */
public fun resolveIcon(key: String): ImageVector =
    iconCatalogue.firstOrNull { it.first == key }?.second ?: HardDrive
