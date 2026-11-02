// This module applies the Kotlin JVM plugin directly rather than a convention plugin, the same
// deviation :desktop-application documents: the library convention wires a Compose/lifecycle
// dependency set that the MCP endpoint has no use for. Its whole classpath is the handoff
// contract, serialization and the JDK's own HTTP server.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.dataHandoffApi)
    implementation(libs.kotlinx.serialization.json)
    // :data-handoff-api exposes coroutines as `implementation`, so it reaches this module's
    // runtime classpath but not its compile classpath — and the HTTP handler needs to bridge a
    // blocking exchange onto the dispatcher's suspend API.
    implementation(libs.kotlinx.coroutines.core)
    // kotlin.test is wired once in the Library/Feature convention plugins; this module has
    // neither, so it must declare its own. Sanctioned exception, see testing.md.
    testImplementation(kotlin("test"))
}
