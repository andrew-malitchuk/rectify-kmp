package dev.rectify.io.convention.source.convention.library

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import dev.rectify.io.convention.core.ext.libs
import dev.rectify.io.convention.core.ext.moduleName
import dev.rectify.io.convention.source.convention.base.BaseConventionPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for non-UI library modules (`data-*`, `domain-*`, `presentation-core-*`).
 *
 * Applies the AGP 9.0 `com.android.kotlin.multiplatform.library` plugin together with
 * the KMP plugin. Registers Android and iOS (arm64, simulatorArm64) targets and wires
 * coroutines and lifecycle libraries to `commonMain`.
 *
 * Library modules intentionally omit Compose dependencies — use [FeatureConventionPlugin]
 * for modules that render UI. `androidResources` is also disabled here since pure data/domain
 * modules do not need Android resource processing.
 *
 * @see FeatureConventionPlugin
 * @see ApplicationConventionPlugin
 */
class LibraryConventionPlugin : BaseConventionPlugin() {

    /**
     * Applies KMP and Android multiplatform library plugins.
     *
     * Compose plugins are deliberately omitted — this plugin targets non-UI modules.
     */
    override fun Project.configurePlugin() = with(project.pluginManager) {
        apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
        apply(libs.findPlugin("androidMultiplatformLibrary").get().get().pluginId)
    }

    /**
     * Configures the Android KMP library target with SDK versions and JVM 21 compiler options.
     *
     * Uses [KotlinMultiplatformAndroidLibraryTarget] — the AGP 9.0 DSL replacement for
     * the legacy `android {}` block. `androidResources` is intentionally not enabled here.
     */
    override fun Project.configureAndroidPlatform() {
        val minSdkVersion = libs.findVersion("minSdk").get().requiredVersion.toInt()
        val targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
                namespace = moduleName
                compileSdk = targetSdk
                minSdk = minSdkVersion
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    /**
     * Registers iOS targets with a static framework whose `baseName` is derived from [moduleName].
     *
     * NOTE: `androidTarget()` is registered automatically by the
     * `com.android.kotlin.multiplatform.library` plugin and must not be called manually.
     */
    override fun Project.configureIOsPlatform() {
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            listOf(
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = moduleName.replace(".", "-")
                    isStatic = true
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
     * Adds coroutines and lifecycle libraries to `commonMain`, and `kotlin-test` to `commonTest`.
     *
     * Enforces explicit API mode for all declarations in this module.
     *
     * @see <a href="https://slack-chats.kotlinlang.org/t/23173883/web-target-it-works-with-ios-android-desktop-but-now-my-wasm">Slack discussion on Compose multiplatform target wiring</a>
     */
    override fun Project.configureCommonDependencies() {
        extensions.getByType<KotlinMultiplatformExtension>().apply {
            explicitApi()
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                    implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
                    implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
                }
                commonTest.dependencies {
                    implementation(libs.findLibrary("kotlin-test").get())
                }
            }
        }
    }

}
