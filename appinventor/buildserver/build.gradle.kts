plugins {
    application
}

application {
    mainClass.set("com.google.appinventor.buildserver.BuildServer")
}

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
        resources {
//            setSrcDirs(projects.components.artifacts)
        }
    }

    test {
        java {
            setSrcDirs(listOf("tests"))
        }
    }
}

dependencies {
    implementation(projects.commonUtils)
    implementation(projects.commonVersion)
    implementation(projects.componentConstants)
    implementation(fileTree("lib") { include("*.jar") })
//    implementation("com.sun.jersey:jersey-grizzly2:1.19.1")
//    implementation("com.sun.jersey:jersey-json:1.19.1")
    implementation("org.json:json:20220320")
    implementation("args4j:args4j:2.0.22")
    implementation("com.google.guava:guava:14.0.1")
    implementation("commons-io:commons-io:2.0.1")
    implementation("org.bouncycastle:bcprov-jdk15on:1.49")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.49")
    implementation("org.eclipse.jdt.core.compiler:ecj:4.5.1")
    implementation("com.android.tools:common:24.3.0")
    implementation("com.android.tools:sdk-common:24.3.0")
    implementation("com.android.tools:sdklib:24.3.0")
    implementation("com.android.tools.build:builder:1.3.0")
    implementation("com.android.tools.build:builder-model:1.3.0")
}