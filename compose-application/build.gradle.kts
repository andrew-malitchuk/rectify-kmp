plugins {
    id("dev.rectify.io.convention.application")
    id("dev.rectify.io.convention.di")
}

// The window icon is loaded from the classpath at runtime, and `icons/` is the one place the
// generated artwork lives — adding it as a resource root beats keeping a second copy of the
// PNG here and letting the two drift. PNG only: the .svg/.icns/.ico beside it are build-time
// inputs with no business in the runtime jar.
sourceSets["main"].resources.srcDir(rootProject.file("icons"))
tasks.named<ProcessResources>("processResources") {
    include("**/*.png")
}

dependencies {
    implementation(compose.material3)
    implementation(libs.kermit)

    implementation(projects.domainCore)
    implementation(projects.dataHandoffApi)
    implementation(projects.dataMcpImpl)
    implementation(projects.dataPreferenceImpl)
    implementation(projects.dataDatabaseImpl)
    implementation(projects.dataDeviceImpl)
    implementation(projects.dataRepositoryImpl)
    implementation(projects.domainUsecaseImpl)
    implementation(projects.presentationCoreLocalisation)
    implementation(projects.presentationCoreStyling)
    implementation(projects.presentationCoreUi)
    implementation(projects.presentationFeatureMirror)
}


// The one place the product version becomes Kotlin. Generated rather than hand-written so the
// status bar and the version the MCP endpoint reports can never drift from what was packaged —
// they were "Rectify 0.4" against a 0.2.0 release until this existed.
val generateVersion = tasks.register("generateRectifyVersion") {
    val version = libs.versions.versionName.get()
    val outputDir = layout.buildDirectory.dir("generated/version")
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("dev/rectify/io/core/version/RectifyVersion.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package dev.rectify.io.core.version

            /** Generated from `versionName` in the version catalogue. Do not edit. */
            internal const val RECTIFY_VERSION: String = "$version"

            """.trimIndent(),
        )
    }
}
sourceSets["main"].kotlin.srcDir(generateVersion)
