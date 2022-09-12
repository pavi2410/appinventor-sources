plugins {
    `java-library`
//    id("org.docstr.gwt") version "1.1.21"
}

//gwt {
//    gwtVersion = "2.8.1"
//}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src"))
        }
    }

    test {
        java {
            setSrcDirs(listOf("tests"))
        }
    }
}

dependencies {
    implementation("com.google.guava:guava:14.0.1")
}