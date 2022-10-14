rootProject.name = "appinventor"
include("appengine-shared")
include("appengine-frontend")
include("appengine-backend")
include("blocklyeditor")
include("common-utils")
include("common-version")
include("component-constants")
include("component-scripts")
include("component-annotations")
include("components")
include("buildserver")
include("barcode")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.google.cloud.tools.appengine")) {
                useModule("com.google.cloud.tools:appengine-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
