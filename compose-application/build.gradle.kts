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

