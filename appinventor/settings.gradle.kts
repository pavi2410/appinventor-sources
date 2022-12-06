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
    plugins {
        id("org.docstr.gwt") version "1.1.22" apply false
        id("org.docstr.gwt.base") version "1.1.22" apply false
        id("org.docstr.gwt.compiler") version "1.1.22" apply false
        id("org.docstr.gwt.war") version "1.1.22" apply false

        id("com.android.application") version "7.3.0" apply false
        id("com.android.library") version "7.3.0" apply false
    }
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
