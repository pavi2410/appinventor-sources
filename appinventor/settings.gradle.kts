rootProject.name = "appinventor"
include("appengine")
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
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}