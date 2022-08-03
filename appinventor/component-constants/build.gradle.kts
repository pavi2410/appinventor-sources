plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
}

tasks.withType(JavaCompile::class).forEach {
    // add utf-8 encoding to all source files
    it.options.encoding = "UTF-8"
}