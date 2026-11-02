rootProject.name = "rectify-kmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
    }
}

include(":compose-application")
include(":desktop-application")
include(":docs-generator")

// common
include(":common-core")

// data
include(":data-core")
include(":data-handoff-api")
include(":data-mcp-impl")
include(":data-device-api")
include(":data-device-impl")
include(":data-preference-api")
include(":data-preference-impl")
include(":data-database-api")
include(":data-database-impl")
include(":data-repository-impl")

// domain
include(":domain-core")
include(":domain-repository-api")
include(":domain-usecase-api")
include(":domain-usecase-impl")

// presentation-core
include(":presentation-core-localisation")
include(":presentation-core-styling")
include(":presentation-core-ui")

// presentation-feature
include(":presentation-feature-mirror")
