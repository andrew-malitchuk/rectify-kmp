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
            implementation(compose.material3)

            implementation(projects.domainCore)
            implementation(projects.domainUsecaseApi)
            implementation(projects.presentationCoreLocalisation)
            implementation(projects.presentationCoreStyling)
            implementation(projects.presentationCoreUi)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
