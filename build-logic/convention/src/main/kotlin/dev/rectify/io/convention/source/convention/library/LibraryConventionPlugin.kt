package dev.rectify.io.convention.source.convention.library

import dev.rectify.io.convention.core.ext.JVM_TOOLCHAIN
import dev.rectify.io.convention.core.ext.implementation
import dev.rectify.io.convention.core.ext.libs
import dev.rectify.io.convention.core.ext.testImplementation
import dev.rectify.io.convention.source.convention.base.BaseConventionPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Convention plugin for non-UI library modules (`data-*`, `domain-*`, `presentation-core-*`).
 *
 * Applies the Kotlin JVM plugin and wires coroutines and lifecycle libraries, plus
 * `kotlin-test` on `test`.
 *
 * Library modules intentionally omit Compose dependencies — use
 * [dev.rectify.io.convention.source.convention.feature.FeatureConventionPlugin] for
 * modules that render UI.
 *
 * @see dev.rectify.io.convention.source.convention.feature.FeatureConventionPlugin
 * @see dev.rectify.io.convention.source.convention.application.ApplicationConventionPlugin
 */
class LibraryConventionPlugin : BaseConventionPlugin() {

    /**
     * Applies the Kotlin JVM plugin.
     *
     * Compose plugins are deliberately omitted — this plugin targets non-UI modules.
     */
    override fun Project.configurePlugin() = with(pluginManager) {
        apply(libs.findPlugin("kotlinJvm").get().get().pluginId)
        apply("java-library")
    }

    /**
     * Pins the JVM toolchain and enforces explicit API mode for all declarations.
     */
    override fun Project.configureJvm() {
        extensions.getByType<KotlinJvmProjectExtension>().apply {
            jvmToolchain(JVM_TOOLCHAIN)
            explicitApi()
        }
    }

    /**
     * Adds coroutines and lifecycle libraries, and `kotlin-test` to the `test` source set.
     */
    override fun Project.configureDependencies() {
        implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
        implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())

        testImplementation(libs.findLibrary("kotlin-test").get())
    }

}
