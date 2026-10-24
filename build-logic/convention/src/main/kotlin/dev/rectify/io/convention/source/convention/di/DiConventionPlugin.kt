package dev.rectify.io.convention.source.convention.di

import dev.rectify.io.convention.core.ext.api
import dev.rectify.io.convention.core.ext.implementation
import dev.rectify.io.convention.core.ext.libs
import dev.rectify.io.convention.source.convention.base.BaseConventionPlugin
import org.gradle.api.Project

/**
 * Convention plugin that wires Koin dependency injection libraries into a module.
 *
 * Designed to be applied **on top of**
 * [dev.rectify.io.convention.source.convention.library.LibraryConventionPlugin] or
 * [dev.rectify.io.convention.source.convention.feature.FeatureConventionPlugin] — never
 * standalone. It applies no Gradle plugin IDs; it only contributes dependencies.
 *
 * `koin-core` is exposed via `api()` so that consumers of a module using this plugin
 * receive the Koin contract transitively without an explicit dependency declaration.
 * `koin-compose` and `koin-viewmodel` are `implementation()` — modules that need them
 * must apply this plugin directly.
 */
class DiConventionPlugin : BaseConventionPlugin() {

    /**
     * Adds `koin-core` (api), `koin-compose`, and `koin-viewmodel`.
     *
     * NOTE: `koin-core` is declared as `api()` intentionally — it is a shared contract
     * that consumers need at compile time (e.g. to call `koinInject()` or `get()`).
     * Compose and ViewModel Koin integrations are `implementation()` because they are
     * not part of the public module API.
     */
    override fun Project.configureDependencies() {
        api(libs.findLibrary("koin-core").get())
        implementation(libs.findLibrary("koin-compose").get())
        implementation(libs.findLibrary("koin-viewmodel").get())
    }
}
