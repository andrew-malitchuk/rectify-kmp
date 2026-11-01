plugins {
    id("dev.rectify.io.convention.feature")
    id("dev.rectify.io.convention.di")
}

dependencies {
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
    implementation(libs.orbit.core)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)
    implementation(compose.material3)

    implementation(projects.domainCore)
    implementation(projects.domainUsecaseApi)
    implementation(projects.dataHandoffApi)
    implementation(projects.presentationCoreLocalisation)
    implementation(projects.presentationCoreStyling)
    implementation(projects.presentationCoreUi)

    testImplementation(libs.kotlinx.coroutines.test)
    // Test-only: `ImageBitmap(w, h)` needs Skiko's native library, which only
    // `:compose-application` pulls in via `compose.desktop.currentOs`. Without it
    // `ThumbnailBitmapTest` dies on `LibraryLoadException` instead of measuring a real
    // thumbnail. Production code is unaffected.
    testImplementation(compose.desktop.currentOs)
}
