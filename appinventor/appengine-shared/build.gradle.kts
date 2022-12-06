plugins {
    `java-library`
    id("org.docstr.gwt.base")
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

gwt {
    gwtVersion = "2.10.0"
    modules("com.google.appinventor.shared.AppengineShared")
}

dependencies {
    api(projects.commonUtils)
    api(projects.componentConstants)
    api("com.google.guava:guava-gwt:20.0")
}