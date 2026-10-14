plugins {
    id("dev.rectify.io.convention.feature")
}

compose.resources {
    publicResClass = true
}

kotlin {
    sourceSets {
        commonMain.dependencies {
        }
    }
}
