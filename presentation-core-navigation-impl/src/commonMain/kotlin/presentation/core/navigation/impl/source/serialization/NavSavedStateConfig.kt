package presentation.core.navigation.impl.source.serialization

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import presentation.core.navigation.api.source.destination.Destination

/**
 * Saved-state serialization configuration for Navigation 3.
 *
 * Registers all [Destination] subclasses as polymorphic subtypes of [NavKey]
 * so the back stack can survive process death. New destinations must be
 * registered here to enable state restoration.
 *
 * @see Destination
 * @see NavigationHost
 */
public val navSavedStateConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclass(Destination.Splash::class, Destination.Splash.serializer())
                subclass(Destination.Onboarding::class, Destination.Onboarding.serializer())
                subclass(Destination.Home::class, Destination.Home.serializer())
                subclass(Destination.Settings::class, Destination.Settings.serializer())
                subclass(Destination.About::class, Destination.About.serializer())
                subclass(Destination.Styleguide::class, Destination.Styleguide.serializer())
                subclass(Destination.LanguagePicker::class, Destination.LanguagePicker.serializer())
            }
        }
    }
