plugins {
    id("dev.rectify.io.convention.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domainCore)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
