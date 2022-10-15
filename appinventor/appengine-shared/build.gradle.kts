plugins {
    `java-library`
    id("org.docstr.gwt") version "1.1.21" // TODO: should apply gwt-base
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

gwt {
    gwtVersion = "2.10.0"
}

dependencies {
    api(projects.commonUtils)
    api(projects.componentConstants)
    api("com.google.guava:guava-gwt:20.0")
}