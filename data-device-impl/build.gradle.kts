plugins {
    id("dev.rectify.io.convention.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dataDeviceApi)
            implementation(projects.dataCore)
            implementation(libs.koin.core)
            implementation(libs.kermit)
        }
        // Contract only, and desktop only: `ToolResolver` reads the user-chosen `adb` path
        // through data-preference-api. Never data-preference-impl - this module must not know
        // how a preference is stored.
        val desktopMain by getting {
            dependencies {
                implementation(projects.dataPreferenceApi)
            }
        }
        // Desktop-only: the transport under test is the desktopMain capture loop, and the
        // fake executor driving it needs virtual time.
        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
