plugins {
    `java-library`
    id("org.docstr.gwt.base")
}

gwt {
    gwtVersion = "2.10.0"
    modules("com.google.appinventor.common.CommonUtils")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("com.google.guava:guava:14.0.1")
}