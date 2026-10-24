package dev.rectify.io.convention.source.convention.application

import dev.rectify.io.convention.core.ext.JVM_TOOLCHAIN
import dev.rectify.io.convention.core.ext.composeDependencies
import dev.rectify.io.convention.core.ext.implementation
import dev.rectify.io.convention.core.ext.libs
import dev.rectify.io.convention.source.convention.base.BaseConventionPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Convention plugin for the shared application library module (`compose-application`).
 *
 * Applies the Kotlin JVM and Compose Multiplatform plugins and wires the full
 * Compose + coroutines + lifecycle dependency set, plus `compose.desktop.currentOs`
 * (the Skiko renderer) and `kotlinx-coroutines-swing`.
 *
 * The runnable entry point lives in the separate `:desktop-application` module; this
 * plugin is intentionally a *library* from Gradle's perspective.
 *
 * @see dev.rectify.io.convention.source.convention.feature.FeatureConventionPlugin
 * @see dev.rectify.io.convention.source.convention.library.LibraryConventionPlugin
 */
class ApplicationConventionPlugin : BaseConventionPlugin() {

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
     * coroutines and lifecycle libraries, plus the desktop-only Skiko renderer
     * (`compose.desktop.currentOs`) and the Swing-backed main dispatcher.
     *
     * Without `currentOs`, Compose has no renderer for the host platform and the
     * `application {}` entry point in `:desktop-application` cannot open a window.
     */
    override fun Project.configureDependencies() {
        val compose = composeDependencies
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.ui)
        implementation(compose.components.resources)
        implementation(compose.components.uiToolingPreview)
        implementation(compose.desktop.currentOs)

        implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        implementation(libs.findLibrary("kotlinx-coroutines-swing").get())
        implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
        implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
    }

}
