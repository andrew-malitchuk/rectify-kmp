import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.composeApplication)
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "dev.rectify.io.source.entry.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            // jpackage builds the bundled runtime from the modules it can SEE on the
            // classpath, and the MCP endpoint reaches for `com.sun.net.httpserver` by
            // reflection-free import only at runtime. Without this the packaged app opens its
            // window and then cannot serve the protocol at all — a failure that never shows up
            // in `./gradlew run`, which uses the full system JDK.
            modules("jdk.httpserver")
            packageName = "Rectify"
            packageVersion = "0.2.0"

            // One source SVG, three platform formats — see `icons/` and
            // `.claude/rules/icon-branding-placeholder.md` for how to regenerate them.
            //
            // `macOS.iconFile` also drives the DOCK icon of `./gradlew run`: the Compose
            // plugin passes it as `-Xdock:icon` itself. Adding our own `jvmArgs` entry for
            // that is not just redundant, it LOSES — a second -Xdock:icon wins over the
            // first, so a hand-rolled one replaces this multi-resolution .icns with
            // whatever single raster it names.
            macOS {
                iconFile.set(rootProject.file("icons/rectify.icns"))
                // jpackage refuses a macOS app-version whose first component is 0, so the
                // bundle carries 1.0.0 while the release itself is 0.1.0. This is Apple's
                // CFBundleShortVersionString — an ordering field for the OS, not the product
                // version. The git tag and CHANGELOG.md are the source of truth.
                packageVersion = "1.0.0"
            }
            windows { iconFile.set(rootProject.file("icons/rectify.ico")) }
            linux { iconFile.set(rootProject.file("icons/rectify.png")) }
        }
    }
}
