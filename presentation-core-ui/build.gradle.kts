plugins {
    id("dev.rectify.io.convention.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.stately.collections)
            implementation(libs.atomicfu)
            implementation(compose.material3)

            implementation(projects.presentationCoreStyling)
            implementation(libs.kermit)
        }
    }
}
