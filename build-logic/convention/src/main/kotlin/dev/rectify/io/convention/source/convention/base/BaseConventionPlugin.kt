package dev.rectify.io.convention.source.convention.base

import dev.rectify.io.convention.core.ext.moduleName
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Abstract base for all convention plugins — implements the Template Method pattern
 * over a fixed set of configuration hooks.
 *
 * Every module in this project is a plain JVM module (`kotlin("jvm")`) targeting the
 * desktop. Subclasses override only the hooks relevant to their module type; all other
 * hooks default to no-ops. The [apply] sequence is fixed and always runs in the same
 * order: plugin application → JVM toolchain → dependencies.
 *
 * Hook execution order in [apply]:
 * 1. [configurePlugin] — apply Gradle plugin IDs
 * 2. [configureJvm] — JVM toolchain and explicit API mode
 * 3. [configureDependencies] — `main`/`test` dependencies
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
            // extension (KotlinJvmProjectExtension, ComposeExtension) is accessed in the
            // hooks below.
            configurePlugin()
            configureJvm()
            configureDependencies()
        }
    }

    /**
     * Hook for applying Gradle plugin IDs via [Project.pluginManager].
     *
     * Override to apply `kotlinJvm`, `composeMultiplatform`, or any other plugin required
     * by the module type. Must complete before any extension block is accessed.
     */
    open fun Project.configurePlugin() = Unit

    /**
     * Hook for the JVM toolchain and explicit API mode.
     *
     * Override to call `jvmToolchain(21)` and `explicitApi()`.
     */
    open fun Project.configureJvm() = Unit

    /**
     * Hook for adding dependencies to the `main` and `test` source sets.
     *
     * Override to declare the module type's shared libraries.
     */
    open fun Project.configureDependencies() = Unit

}
