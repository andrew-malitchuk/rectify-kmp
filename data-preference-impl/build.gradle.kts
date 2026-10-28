plugins {
    id("dev.rectify.io.convention.library")
}

dependencies {
    api(projects.dataPreferenceApi)
    implementation(projects.dataCore)
    implementation(libs.multiplatform.settings)
    implementation(libs.multiplatform.settings.coroutines)
    implementation(libs.koin.core)

    testImplementation(libs.multiplatform.settings.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
