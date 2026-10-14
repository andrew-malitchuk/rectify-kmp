package dev.rectify.io.convention.source.convention.di

import dev.rectify.io.convention.core.ext.libs
import dev.rectify.io.convention.source.convention.base.BaseConventionPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin that wires Koin dependency injection libraries into a KMP module.
 *
 * Designed to be applied **on top of** [LibraryConventionPlugin] or
 * [FeatureConventionPlugin] — never standalone. It does not apply any Gradle plugin IDs
 * or register platform targets; it only contributes dependencies to existing source sets.
 *
 * `koin-core` is exposed via `api()` so that consumers of a module using this plugin
 * receive the Koin contract transitively without an explicit dependency declaration.
 * `koin-compose` and `koin-viewmodel` are `implementation()` — modules that need them
 * must apply this plugin directly.
 *
 * @see LibraryConventionPlugin
 * @see FeatureConventionPlugin
 */
class DiConventionPlugin : BaseConventionPlugin() {

    /**
     * Adds `koin-core` (api), `koin-compose`, and `koin-viewmodel` to `commonMain`.
     *
     * NOTE: `koin-core` is declared as `api()` intentionally — it is a shared contract
     * that consumers need at compile time (e.g. to call `koinInject()` or `get()`).
     * Compose and ViewModel Koin integrations are `implementation()` because they are
     * not part of the public module API.
     */
    override fun Project.configureCommonDependencies() {
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            sourceSets.apply {
                commonMain.dependencies {
                    api(libs.findLibrary("koin-core").get())
                    implementation(libs.findLibrary("koin-compose").get())
                    implementation(libs.findLibrary("koin-viewmodel").get())
                }
            }
        }
    }

    /**
     * Adds `koin-android` and `koin-android-compose` to `androidMain`.
     */
    override fun Project.configureAndroidDependencies() {
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            sourceSets.apply {
                androidMain.dependencies {
                    implementation(libs.findLibrary("koin-android").get())
                    implementation(libs.findLibrary("koin-android-compose").get())
                }
            }
        }
    }
}
