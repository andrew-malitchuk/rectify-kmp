package dev.rectify.io.convention.core.ext

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin

/** The single JVM toolchain every module in this project compiles against. */
internal const val JVM_TOOLCHAIN: Int = 21

/**
 * Retrieves the `libs` [VersionCatalog] registered in this project's settings.
 *
 * Provides access to versions, libraries, and plugins declared in
 * `gradle/libs.versions.toml` from inside a convention plugin, where the generated
 * type-safe `libs` accessor is not available.
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * The Compose Multiplatform dependency factory (`compose.runtime`, `compose.desktop`, …).
 *
 * Only valid after the `org.jetbrains.compose` plugin has been applied — i.e. from
 * `configureDependencies()`, never from `configurePlugin()`.
 */
internal val Project.composeDependencies: ComposePlugin.Dependencies
    get() = extensions.getByType<ComposeExtension>().dependencies

/** Adds [dependency] to the `implementation` configuration. */
internal fun Project.implementation(dependency: Any) {
    dependencies.add("implementation", dependency)
}

/** Adds [dependency] to the `api` configuration. */
internal fun Project.api(dependency: Any) {
    dependencies.add("api", dependency)
}

/** Adds [dependency] to the `testImplementation` configuration. */
internal fun Project.testImplementation(dependency: Any) {
    dependencies.add("testImplementation", dependency)
}

/**
 * Derives a dot-separated module name from the Gradle project [path].
 *
 * Strips the leading colon and converts hyphens to dots.
 *
 * Examples:
 * - `:feature-login` → `feature.login`
 * - `:data-ble-impl` → `data.ble.impl`
 *
 * NOTE: Returns an empty string for the root project (path `":"`).
 * Convention plugins must never be applied to the root project.
 */
internal val Project.moduleName: String
    get() = path.replace(":", "").replace("-", ".")
