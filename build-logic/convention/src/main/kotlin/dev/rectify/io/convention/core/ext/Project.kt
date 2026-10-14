package dev.rectify.io.convention.core.ext

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Retrieves the `libs` [VersionCatalog] registered in this project's settings.
 *
 * Provides type-safe access to versions, libraries, and plugins declared in
 * `gradle/libs.versions.toml` without relying on string-based lookups at
 * the call site.
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Derives a dot-separated module name from the Gradle project [path].
 *
 * Strips the leading colon and converts hyphens to dots so that the result
 * can be used directly as an Android namespace or iOS framework `baseName`.
 *
 * Examples:
 * - `:feature-login` → `feature.login`
 * - `:data-ble-impl` → `data.ble.impl`
 *
 * NOTE: Returns an empty string for the root project (path `":"`).
 * Convention plugins must never be applied to the root project — doing so
 * would produce an empty Android namespace and fail the build with an
 * unhelpful error message.
 */
internal val Project.moduleName: String
    get() = path.replace(":", "").replace("-", ".")
