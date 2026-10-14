plugins {
    id("dev.rectify.io.convention.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.dataCore)
        }
    }
}
