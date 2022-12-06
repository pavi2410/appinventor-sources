plugins {
    `java-library`
    id("org.docstr.gwt.base")
}

gwt {
    gwtVersion = "2.10.0"
    modules("com.google.appinventor.components.CommonConstants")
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
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}