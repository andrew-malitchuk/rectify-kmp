plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.composeApplication)
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "dev.rectify.io.source.entry.MainKt"
    }
}
