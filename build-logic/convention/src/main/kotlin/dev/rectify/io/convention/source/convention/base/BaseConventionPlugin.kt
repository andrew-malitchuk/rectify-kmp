package dev.rectify.io.convention.source.convention.base

import dev.rectify.io.convention.core.ext.moduleName
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Abstract base for all convention plugins — implements the Template Method pattern
 * over a fixed set of platform and dependency configuration hooks.
 *
 * Subclasses override only the hooks relevant to their module type; all other hooks
 * default to no-ops. The [apply] sequence is fixed and always runs in the same order:
 * plugin application → platform targets → dependencies.
 *
 * Hook execution order in [apply]:
 * 1. [configurePlugin] — apply Gradle plugin IDs
 * 2. [configureAndroidPlatform] — Android KMP target
 * 3. [configureIOsPlatform] — iOS targets (x64, arm64, simulatorArm64)
 * 4. [configureDesktopPlatform] — `jvm("desktop")` target
 * 5. [configureCommonDependencies] — `commonMain` source set dependencies
 * 6. [configureAndroidDependencies] — `androidMain` source set dependencies
 * 7. [configureIOsDependencies] — `iosMain` source set dependencies
 * 8. [configureDesktopDependencies] — `desktopMain` source set dependencies
 */
open class BaseConventionPlugin : Plugin<Project> {

    /**
     * Applies the convention plugin to [target], logging the module path and name,
     * then invoking all configuration hooks in their fixed order.
     *
     * @param target The Gradle [Project] this plugin is applied to.
     */
    override fun apply(target: Project) {
        target.logger.lifecycle("> Applied convention plugin for module: ${target.path}")
        with(target) {
            logger.lifecycle("> Module name: $moduleName")

            // NOTE: Hook order is load-bearing. Plugin IDs must be applied before any
            // extension (KotlinMultiplatformExtension, ComposeExtension, etc.) is accessed
            // in the platform and dependency hooks below.
            configurePlugin()

            configureAndroidPlatform()
            configureIOsPlatform()
            configureDesktopPlatform()

            configureCommonDependencies()
            configureAndroidDependencies()
            configureIOsDependencies()
            configureDesktopDependencies()
        }
    }

    /**
     * Hook for applying Gradle plugin IDs via [Project.pluginManager].
     *
     * Override to apply `kotlinMultiplatform`, `androidMultiplatformLibrary`,
     * `composeMultiplatform`, or any other plugin required by the module type.
     * Must complete before any extension block is accessed.
     */
    open fun Project.configurePlugin() = Unit

    /**
     * Hook for configuring the Android KMP library target.
     *
     * Override to set `namespace`, `compileSdk`, `minSdk`, `androidResources`,
     * and compiler options via [com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget].
     */
    open fun Project.configureAndroidPlatform() = Unit

    /**
     * Hook for registering iOS targets: `iosArm64`, `iosSimulatorArm64`.
     *
     * Override to configure framework name, linkage mode (`isStatic`), and
     * any iOS-specific binary options.
     */
    open fun Project.configureIOsPlatform() = Unit

    /**
     * Hook for registering the `jvm("desktop")` target.
     *
     * Override to configure the desktop JVM target's compiler options (JVM toolchain,
     * `jvmTarget`). The resulting source set is named `desktopMain`/`desktopTest`.
     */
    open fun Project.configureDesktopPlatform() = Unit

    /**
     * Hook for adding dependencies to the `commonMain` source set.
     *
     * Override to declare libraries shared across all platforms (coroutines,
     * Compose runtime, lifecycle, etc.). Call `explicitApi()` here if the module
     * enforces explicit visibility modifiers.
     */
    open fun Project.configureCommonDependencies() = Unit

    /**
     * Hook for adding dependencies to the `androidMain` source set.
     *
     * Override to declare Android-only libraries such as Koin Android,
     * Compose UI tooling, or `androidRuntimeClasspath` entries.
     */
    open fun Project.configureAndroidDependencies() = Unit

    /**
     * Hook for adding dependencies to the `iosMain` source set.
     *
     * Override to declare iOS-only libraries. Left as no-op in most module types
     * since KMP shared libraries rarely need iOS-exclusive dependencies.
     */
    open fun Project.configureIOsDependencies() = Unit

    /**
     * Hook for adding dependencies to the `desktopMain` source set.
     *
     * Override to declare desktop-only libraries, such as `compose.desktop.currentOs`
     * for the module that hosts the application entry point.
     */
    open fun Project.configureDesktopDependencies() = Unit

}
