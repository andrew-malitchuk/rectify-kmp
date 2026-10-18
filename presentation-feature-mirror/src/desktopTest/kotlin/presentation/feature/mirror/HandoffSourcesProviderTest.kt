package presentation.feature.mirror

import data.handoff.api.source.handoff.AnnotationHandoff
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import presentation.feature.mirror.source.mirror.CropEncoder
import presentation.feature.mirror.source.mirror.provideHandoffSources
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * TAC-2 — the desktop graph resolves both [AnnotationHandoff] and [CropEncoder] (AS-29/AS-30).
 *
 * Resolution only, following `DeviceSourcesModuleTest`'s precedent: constructing these spawns no
 * process and touches no real file, so a missing registration is a RUNTIME failure that
 * compiling the module cannot catch.
 */
class HandoffSourcesProviderTest {

    @Test
    fun annotationHandoffAndCropEncoderBothResolveOnDesktop() {
        val koin = koinApplication {
            modules(module { provideHandoffSources() })
        }.koin

        assertNotNull(koin.get<AnnotationHandoff>())
        assertNotNull(koin.get<CropEncoder>())
    }
}
