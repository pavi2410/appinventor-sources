plugins {
    java
    war
    id("com.google.cloud.tools.appengine") version "2.4.4"
}

group = "com.google.appinventor"
version = "unspecified"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

appengine {
    deploy {
        version = "1"
        projectId = "your GCP project ID"
    }
    run {

    }
}

dependencies {
}
