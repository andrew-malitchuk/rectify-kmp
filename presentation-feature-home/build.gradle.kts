plugins {
    id("dev.rectify.io.convention.feature")
    id("dev.rectify.io.convention.di")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.orbit.core)
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)

            implementation(projects.presentationCoreLocalisation)
            implementation(projects.presentationCoreNavigationApi)
            implementation(projects.presentationCoreStyling)
            implementation(projects.presentationCoreUi)
        }
    }
}
