package dev.rectify.io.convention.source.convention.application

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import dev.rectify.io.convention.core.ext.libs
import dev.rectify.io.convention.core.ext.moduleName
import dev.rectify.io.convention.source.convention.base.BaseConventionPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for the shared KMP application library module (`compose-application`).
 *
 * Applies the AGP 9.0 `com.android.kotlin.multiplatform.library` plugin (replacing the
 * deprecated `com.android.application`) together with KMP and Compose Multiplatform plugins.
 * Registers Android and iOS (arm64, simulatorArm64) targets and wires the full
 * Compose + coroutines + lifecycle dependency set to `commonMain`.
 *
 * The Android application entry point lives in the separate `:aos-application` module;
 * this plugin is intentionally a *library* from AGP's perspective.
 *
 * @see FeatureConventionPlugin
 * @see LibraryConventionPlugin
 */
class ApplicationConventionPlugin : BaseConventionPlugin() {

    /**
     * Applies KMP, Android multiplatform library, Compose Multiplatform,
     * and Compose Compiler plugins.
     */
    override fun Project.configurePlugin() = with(project.pluginManager) {
        apply(project.libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
        apply(project.libs.findPlugin("androidMultiplatformLibrary").get().get().pluginId)
        apply(project.libs.findPlugin("composeMultiplatform").get().get().pluginId)
        apply(project.libs.findPlugin("composeCompiler").get().get().pluginId)
    }

    /**
     * Configures the Android KMP library target with SDK versions and JVM 21 compiler options.
     *
     * Uses [KotlinMultiplatformAndroidLibraryTarget] — the AGP 9.0 DSL replacement for the
     * legacy `android {}` / `ApplicationExtension` block. `androidTarget()` is registered
     * automatically by the `com.android.kotlin.multiplatform.library` plugin.
     */
    override fun Project.configureAndroidPlatform() {
        val minSdkVersion = libs.findVersion("minSdk").get().requiredVersion.toInt()
        val targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
                namespace = moduleName
                compileSdk = targetSdk
                minSdk = minSdkVersion
                androidResources { enable = true }
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    /**
     * Registers iOS targets with a static framework named `application_compose`.
     *
     * NOTE: The framework `baseName` is intentionally hardcoded here — the project has only
     * one `compose-application` module. Feature and Library plugins derive `baseName`
     * from [moduleName] instead.
     */
    override fun Project.configureIOsPlatform() {
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            listOf(
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    this.baseName = "application_compose"
                    this.isStatic = true
                }
            }
        }
    }

    /**
     * Registers the `jvm("desktop")` target with JVM 21 compiler options.
     */
    override fun Project.configureDesktopPlatform() {
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            jvm("desktop") {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    /**
     * Adds Compose runtime, foundation, material, UI, resources, UI tooling preview,
     * coroutines, and lifecycle libraries to `commonMain`.
     *
     * Enforces explicit API mode for all declarations in this module.
     *
     * @see <a href="https://slack-chats.kotlinlang.org/t/23173883/web-target-it-works-with-ios-android-desktop-but-now-my-wasm">Slack discussion on Compose multiplatform target wiring</a>
     */
    override fun Project.configureCommonDependencies() {
        val composeDependencies = extensions.getByType<ComposeExtension>().dependencies
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            explicitApi()
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(composeDependencies.runtime)
                    implementation(composeDependencies.foundation)
                    implementation(composeDependencies.material)
                    implementation(composeDependencies.ui)
                    implementation(composeDependencies.components.resources)
                    implementation(composeDependencies.components.uiToolingPreview)

                    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                    implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
                    implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
                }
            }
        }
    }

    /**
     * Adds Compose UI tooling to `androidRuntimeClasspath` for Android Studio preview support.
     *
     * NOTE: `androidRuntimeClasspath` is the correct configuration name for the
     * `com.android.kotlin.multiplatform.library` plugin — `debugImplementation` is
     * not available in the KMP Android library DSL.
     */
    override fun Project.configureAndroidDependencies() {
        val composeDependencies = extensions.getByType<ComposeExtension>().dependencies
        project.dependencies.add("androidRuntimeClasspath", composeDependencies.uiTooling)
    }

    /**
     * Adds `compose.desktop.currentOs` (Skiko native bindings for the running OS) and
     * `kotlinx-coroutines-swing` (Swing-backed main dispatcher) to `desktopMain`.
     *
     * Required for the Window/`application {}` entry point in `:desktop-application` to run —
     * without `currentOs`, Compose Desktop has no renderer for the host platform.
     */
    override fun Project.configureDesktopDependencies() {
        val composeDependencies = extensions.getByType<ComposeExtension>().dependencies
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            sourceSets.getByName("desktopMain").dependencies {
                implementation(composeDependencies.desktop.currentOs)
                implementation(libs.findLibrary("kotlinx-coroutines-swing").get())
            }
        }
    }

}
