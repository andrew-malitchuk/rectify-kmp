plugins {
    id("dev.rectify.io.convention.library")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.domainUsecaseApi)
            implementation(projects.domainCore)
            implementation(projects.domainRepositoryApi)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
    }
}
