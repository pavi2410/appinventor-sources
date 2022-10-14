plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(projects.commonUtils)
    implementation(projects.componentConstants)
    implementation(projects.componentAnnotations)
    implementation("com.google.guava:guava:14.0.1")
    implementation("org.json:json:20220320")
}

tasks.withType(JavaCompile::class).forEach {
    // add utf-8 encoding to all source files
    it.options.encoding = "UTF-8"
}
