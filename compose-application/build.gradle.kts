plugins {
    id("dev.rectify.io.convention.application")
    id("dev.rectify.io.convention.di")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domainCore)
            implementation(projects.dataPreferenceImpl)
            implementation(projects.dataDatabaseImpl)
            implementation(projects.dataRepositoryImpl)
            implementation(projects.domainUsecaseImpl)
            implementation(projects.presentationCoreLocalisation)
            implementation(projects.presentationCoreNavigationApi)
            implementation(projects.presentationCoreNavigationImpl)
            implementation(projects.presentationCoreStyling)
            implementation(projects.presentationCoreUi)
            implementation(projects.presentationFeatureAbout)
            implementation(projects.presentationFeatureHome)
            implementation(projects.presentationFeatureOnboarding)
            implementation(projects.presentationFeatureSettings)
            implementation(projects.presentationFeatureSplash)
        }
    }
}
