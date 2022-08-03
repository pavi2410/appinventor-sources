plugins {
    application
}

application {
    mainClass.set("com.google.appinventor.buildserver.BuildServer.java")
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

println(projects.common.artifacts)

dependencies {
    implementation(projects.common)
    implementation(fileTree("lib") { include("*.jar") })
    implementation("org.json:json:20220320")
    implementation("args4j:args4j:2.0.22")
    implementation("com.google.guava:guava:14.0.1")
    implementation("commons-io:commons-io:2.0.1")
}