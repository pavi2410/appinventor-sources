plugins {
    java
    war
    id("com.google.cloud.tools.appengine-standard") version "2.4.4"
}

group = "com.google.appinventor"
version = "unspecified"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

appengine {
    tools {
        setCloudSdkHome("C:\\Program Files (x86)\\Google\\Cloud SDK\\google-cloud-sdk")
        cloudSdkVersion = "405.0.1"
    }
    deploy {
        version = "1"
        projectId = "your GCP project ID"
    }
}

dependencies {
    implementation(projects.appengineShared)
    implementation("com.google.appengine:appengine-api-1.0-sdk:2.0.9")
    providedCompile("javax.servlet:javax.servlet-api:3.1.0")
}
