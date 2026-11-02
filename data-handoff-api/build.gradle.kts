plugins {
    id("dev.rectify.io.convention.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}

// Deliberately NOT applying the di convention plugin: it exposes koin-core as api(), which
// would land Koin on :data-mcp-impl's classpath for no benefit. The Koin binding lives in
// presentation-feature-mirror.
