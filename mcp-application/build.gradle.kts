// This module applies the Kotlin JVM plugin directly rather than a convention plugin: it is
// a standalone server binary with its own deliberately minimal classpath, not a library in
// the app's dependency graph.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // :data-handoff-api is a library-convention module, and LibraryConventionPlugin wires
    // androidx-lifecycle-viewmodel + androidx-lifecycle-runtimeCompose into EVERY library
    // module. This server needs neither, and the two lifecycle coordinates
    // (androidx.lifecycle and org.jetbrains.androidx.lifecycle) both ship a
    // lifecycle-common-jvm jar, which collides in installDist's flat lib/ dir. Excluding
    // them here both fixes that and restores the pinned envelope+serialization classpath
    // the spawn-time budget assumes.
    implementation(projects.dataHandoffApi) {
        exclude(group = "androidx.lifecycle")
        exclude(group = "org.jetbrains.androidx.lifecycle")
    }
    implementation(libs.kotlinx.serialization.json)
    // kotlin.test is wired once in the Library/Feature convention plugins; this module has
    // neither, so it must declare its own. Sanctioned exception, see testing.md.
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("dev.rectify.io.mcp.source.entry.MainKt")
    applicationName = "rectify-mcp" // installDist -> build/install/rectify-mcp/bin/rectify-mcp
}

// explicitApi() is NOT applied here (no convention plugin) — keep declarations internal by
// default.
