plugins {
    `java-library`
    id("org.docstr.gwt.base")
}

gwt {
    gwtVersion = "2.10.0"
    modules("com.google.appinventor.common.CommonConstants")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}