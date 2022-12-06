plugins {
    `java-library`
    id("org.docstr.gwt.base")
}

gwt {
    gwtVersion = "2.10.0"
    modules("com.google.appinventor.common.CommonUtils")

//    src += files(sourceSets["main"].allJava) + files(sourceSets["main"].resources)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.named<Jar>("jar") {
    from(project.sourceSets.main.get().allSource)
    from(project.sourceSets.main.get().output)
    this.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    implementation("com.google.guava:guava:14.0.1")
}