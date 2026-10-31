plugins {
    id("dev.rectify.io.convention.feature")
}

dependencies {
    implementation(libs.stately.collections)
    implementation(libs.atomicfu)
    implementation(compose.material3)

    implementation(projects.presentationCoreStyling)
    implementation(libs.kermit)
}
