// A build-time tool, not part of the app: it reads `docs/` and writes a static site. No
// convention plugin for the same reason `:mcp-application` skips them — those wire a library's
// shared Compose/Koin/lifecycle dependency set, and none of it belongs in a Markdown renderer.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.jetbrains.markdown)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("dev.rectify.io.docs.MainKt")
}

/**
 * Renders `docs/` into `build/docs-site/`.
 *
 * Wired as its own task rather than leaving callers to `:docs-generator:run --args=...` so the
 * paths live here once — CI and a human run the same command.
 */
val generateDocs by tasks.registering(JavaExec::class) {
    group = "documentation"
    description = "Renders docs/ into build/docs-site/ as a static GitHub Pages site."
    mainClass.set("dev.rectify.io.docs.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    // Defaults to "" so a plain `./gradlew generateDocs` produces a site you can open
    // locally. CI passes the real one: `-PdocsBaseUrl=/rectify-kmp`.
    val baseUrl = providers.gradleProperty("docsBaseUrl").getOrElse("")
    args(
        rootProject.file("docs").absolutePath,
        layout.buildDirectory.dir("docs-site").get().asFile.absolutePath,
        baseUrl,
    )
    inputs.property("docsBaseUrl", baseUrl)
    inputs.dir(rootProject.file("docs"))
    outputs.dir(layout.buildDirectory.dir("docs-site"))
}
