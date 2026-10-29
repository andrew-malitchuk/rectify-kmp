plugins {
    id("dev.rectify.io.convention.library")
}

dependencies {
    api(projects.dataDeviceApi)
    implementation(projects.dataCore)
    implementation(libs.koin.core)
    implementation(libs.kermit)
    // Contract only: `ToolResolver` reads the user-chosen `adb` path through
    // data-preference-api. Never data-preference-impl - this module must not know how a
    // preference is stored.
    implementation(projects.dataPreferenceApi)

    // The transport under test is the capture loop, and the fake executor driving it needs
    // virtual time.
    testImplementation(libs.kotlinx.coroutines.test)
}
