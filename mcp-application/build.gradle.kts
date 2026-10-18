// No convention plugin models a plain-JVM module — all four are built around
// KotlinMultiplatformExtension. :desktop-application set and documented this precedent;
// this extends it by one. Declared, not discovered (NFR-15 deviation).
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.dataHandoffApi) // resolves the jvm("desktop") variant, as :desktop-application already proves
    implementation(libs.kotlinx.serialization.json)
    // kotlin.test is wired once in the Library/Feature convention plugins; this module has
    // neither, so it must declare its own. Sanctioned exception, see testing.md.
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("dev.rectify.io.mcp.source.entry.MainKt")
    applicationName = "rectify-mcp" // installDist -> build/install/rectify-mcp/bin/rectify-mcp
}

// settings.gradle.kts gains include(":mcp-application") beside :desktop-application.
// Zero new version-catalog entries: kotlinJvm, kotlin-serialization and
// kotlinx-serialization-json all already exist and are already used.
// explicitApi() is NOT applied here (no convention plugin) — keep declarations internal by
// default (see mcp-application/CLAUDE.md note in this report for what was verified).
