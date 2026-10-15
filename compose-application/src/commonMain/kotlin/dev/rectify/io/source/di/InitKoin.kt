package dev.rectify.io.source.di

import data.database.impl.di.dataDatabaseImplModule
import data.device.impl.di.dataDeviceImplModule
import data.preference.impl.di.dataPreferenceImplModule
import data.repository.impl.di.dataRepositoryImplModule
import domain.usecase.impl.di.domainUseCaseImplModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import presentation.feature.about.di.presentationFeatureAboutModule
import presentation.feature.home.di.presentationFeatureHomeModule
import presentation.feature.mirror.di.presentationFeatureMirrorModule
import presentation.feature.onboarding.di.presentationFeatureOnboardingModule
import presentation.feature.settings.di.presentationFeatureSettingsModule
import presentation.feature.splash.di.presentationFeatureSplashModule

/**
 * Initializes the Koin dependency injection graph with all application modules.
 *
 * Registers data, domain, and presentation modules in the correct order.
 * Called once during application startup from platform-specific entry points.
 *
 * @param config Optional platform-specific Koin configuration (e.g., Android context).
 *
 * @see doInitKoin
 */
public fun initKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        // NOTE: Module order is significant — domain modules depend on data contracts,
        // and presentation modules depend on domain use cases. Registering out of order
        // causes Koin to throw "No definition found" at the first injection site.
        modules(
            dataPreferenceImplModule,
            dataDatabaseImplModule,
            // Koin resolves lazily, so this position is defensive rather than load-bearing:
            // it keeps the device sources ahead of the repository that consumes them, in the
            // documented data -> domain -> presentation direction.
            dataDeviceImplModule,
            dataRepositoryImplModule,
            domainUseCaseImplModule,
            presentationFeatureAboutModule,
            presentationFeatureHomeModule,
            presentationFeatureOnboardingModule,
            presentationFeatureSettingsModule,
            presentationFeatureSplashModule,
            presentationFeatureMirrorModule,
        )
    }
}
