plugins {
    `java-library`
    id("org.docstr.gwt") version "1.1.21" // TODO: should apply gwt-base
}

gwt {
    gwtVersion = "2.10.0"
    modules("com.google.appinventor.common.CommonVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("com.google.guava:guava:14.0.1")
}