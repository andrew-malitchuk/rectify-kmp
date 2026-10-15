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
        // Desktop-only: the transport under test is the desktopMain capture loop, and the
        // fake executor driving it needs virtual time.
        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
