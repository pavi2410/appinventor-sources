plugins {
    id("org.docstr.gwt") version "1.1.21"
}

gwt {
    gwtVersion = "2.8.1"
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

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:14.0.1")
}