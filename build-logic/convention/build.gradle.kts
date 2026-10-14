import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Apply the Kotlin DSL plugin for Gradle build logic
plugins {
    `kotlin-dsl`
}

// Set the group for publishing
group = "dev.rectify.io.convention"

// Configure Java compatibility to version 21
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// Set the JVM target for Kotlin compilation
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

// Declare dependencies required for the convention plugins
dependencies {
    implementation(libs.org.jetbrains.kotlin.multiplatform.gradle.plugin)
    implementation(libs.gradle)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.gradle.plugin)
}

// Register custom Gradle plugins for different project types
gradlePlugin {
    plugins {
        // Feature module convention plugin
        register("dev.rectify.io.convention.feature") {
            id = "dev.rectify.io.convention.feature"
            implementationClass =
                "dev.rectify.io.convention.source.convention.feature.FeatureConventionPlugin"
        }
        // Application module convention plugin
        register("dev.rectify.io.convention.application") {
            id = "dev.rectify.io.convention.application"
            implementationClass =
                "dev.rectify.io.convention.source.convention.application.ApplicationConventionPlugin"
        }
        // Library module convention plugin
        register("dev.rectify.io.convention.library") {
            id = "dev.rectify.io.convention.library"
            implementationClass =
                "dev.rectify.io.convention.source.convention.library.LibraryConventionPlugin"
        }
        // Dependency injection convention plugin
        register("dev.rectify.io.convention.di") {
            id = "dev.rectify.io.convention.di"
            implementationClass =
                "dev.rectify.io.convention.source.convention.di.DiConventionPlugin"
        }
    }
}
