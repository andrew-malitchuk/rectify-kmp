package dev.rectify.io.convention.source.convention.feature

import dev.rectify.io.convention.core.ext.JVM_TOOLCHAIN
import dev.rectify.io.convention.core.ext.composeDependencies
import dev.rectify.io.convention.core.ext.implementation
import dev.rectify.io.convention.core.ext.libs
import dev.rectify.io.convention.core.ext.testImplementation
import dev.rectify.io.convention.source.convention.base.BaseConventionPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Convention plugin for UI-layer feature modules (`presentation-feature-*`).
 *
 * Applies the Kotlin JVM and Compose Multiplatform plugins and wires the full
 * Compose + coroutines + lifecycle dependency set, plus `kotlin-test` on `test`.
 *
 * Feature modules differ from
 * [dev.rectify.io.convention.source.convention.library.LibraryConventionPlugin] in that
 * they include Compose dependencies.
 *
 * @see dev.rectify.io.convention.source.convention.library.LibraryConventionPlugin
 * @see dev.rectify.io.convention.source.convention.application.ApplicationConventionPlugin
 */
class FeatureConventionPlugin : BaseConventionPlugin() {

    /**
     * Applies Kotlin JVM, Compose Multiplatform, and Compose Compiler plugins.
     */
    override fun Project.configurePlugin() = with(pluginManager) {
        apply(libs.findPlugin("kotlinJvm").get().get().pluginId)
        apply("java-library")
        apply(libs.findPlugin("composeMultiplatform").get().get().pluginId)
        apply(libs.findPlugin("composeCompiler").get().get().pluginId)
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
     * Adds Compose runtime, foundation, material, UI, resources, UI tooling preview,
     * coroutines and lifecycle libraries, and `kotlin-test` to the `test` source set.
     *
     * NOTE: `compose.desktop.currentOs` is deliberately NOT added here — only the module
     * that actually opens a window needs the Skiko renderer. A feature module that needs
     * it in a test (e.g. to build a real `ImageBitmap`) declares it itself.
     */
    override fun Project.configureDependencies() {
        val compose = composeDependencies
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.ui)
        implementation(compose.components.resources)
        implementation(compose.components.uiToolingPreview)

        implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
        implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())

        testImplementation(libs.findLibrary("kotlin-test").get())
    }

}
