plugins {
    id("dev.rectify.io.convention.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.domainCore)
    implementation(libs.kotlinx.serialization.json)
}
