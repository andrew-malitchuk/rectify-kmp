plugins {
    id("dev.rectify.io.convention.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.dataPreferenceApi)
            implementation(projects.dataCore)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.multiplatform.settings.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.datastore.preferences)
        }
    }
}
