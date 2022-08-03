rootProject.name = "appinventor"
include("appengine")
include("blocklyeditor")
include("common-utils")
include("common-version")
include("component-constants")
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
