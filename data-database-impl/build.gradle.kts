plugins {
    id("dev.rectify.io.convention.library")
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

dependencies {
    api(projects.dataDatabaseApi)
    implementation(projects.dataCore)
    implementation(libs.room.runtime)
    implementation(libs.sqlite.bundled)
    implementation(libs.koin.core)

    ksp(libs.room.compiler)

    testImplementation(libs.kotlinx.coroutines.test)
}

room {
    schemaDirectory("$projectDir/schemas")
}
