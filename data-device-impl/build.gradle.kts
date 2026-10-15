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
    }
}
