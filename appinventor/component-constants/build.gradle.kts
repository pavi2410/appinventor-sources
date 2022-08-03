plugins {
    `java-library`
    id("org.docstr.gwt") version "1.1.21"
}

gwt {
    gwtVersion = "2.8.1"
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