plugins {
    id("dev.rectify.io.convention.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.commonCore)
            implementation(projects.dataCore)
            implementation(projects.dataPreferenceApi)
            implementation(projects.dataDeviceApi)
            implementation(projects.dataDatabaseApi)
            implementation(projects.dataDatabaseImpl)
            implementation(projects.domainCore)
            implementation(projects.domainRepositoryApi)
            implementation(libs.koin.core)
            implementation(libs.kermit)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        // Desktop-only and TEST-only: the composed-graph test registers the real
        // `dataDeviceImplModule` next to this module's own, which is the seam `initKoin()`
        // crosses. Production code here still sees data-device-API only.
        val desktopTest by getting {
            dependencies {
                implementation(projects.dataDeviceImpl)
            }
        }
    }
}
