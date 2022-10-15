plugins {
    id("com.android.library")
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 21
        targetSdk = 30

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
//    println("component constants: ${projects.componentConstants.artifacts}")
    implementation(projects.commonVersion)
    implementation(projects.componentConstants)
    implementation(projects.componentAnnotations)
    implementation(projects.barcode)
    annotationProcessor(projects.componentScripts)
    implementation("androidx.annotation:annotation:1.0.0")
    implementation("androidx.core:core:1.0.0")
    implementation("androidx.appcompat:appcompat:1.0.0")
    implementation(files("../lib/kawa/kawa-1.11-modified.jar"))
    implementation("org.locationtech.jts:jts-core:1.15.0")
    implementation("org.osmdroid:osmdroid-android:6.1.11")
    implementation("com.google.guava:guava:14.0.1")
    implementation("redis.clients:jedis:3.0.0")
    implementation("com.firebase:firebase-client-android:2.5.0")
//    implementation("com.google.api-client:google-api-client:1.10.3-beta")
//    implementation("com.google.api-client:google-api-client-android2:1.10.3-beta")
//    implementation("com.google.http-client:google-http-client:1.10.3-beta")
//    implementation("com.google.http-client:google-http-client-android2:1.10.3-beta")
//    implementation("com.google.http-client:google-http-client-android3:1.10.3-beta")
    implementation(fileTree("../lib/oauth") { include("*.jar") })
    implementation("com.google.apis:google-api-services-fusiontables:v1-rev19-1.13.2-beta")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("org.apache.httpcomponents:httpmime:4.3.2")
    implementation("ch.acra:acra:4.4.0")
//    implementation("org.webrtc:google-webrtc:1.0.19742")
    implementation(files("../lib/webrtc/webrtc.jar"))
    implementation(files("../lib/physicaloid/physicaloid-library.jar"))
    implementation("com.google.protobuf:protobuf-java:3.0.0")
    implementation("org.twitter4j:twitter4j-core:3.0.5")
    implementation("org.twitter4j:twitter4j-media-support:3.0.5")
    implementation("com.caverock:androidsvg-aar:1.4")
    implementation("org.json:json:20220320")
    implementation(files("../lib/mpandroidchart/mpandroidchart.jar"))
    implementation("com.google.api-client:google-api-client-android:1.30.2")
    implementation("com.google.apis:google-api-services-sheets:v4-rev604-1.25.0")
}

// Ref: https://docs.gradle.org/current/userguide/cross_project_publications.html#cross_project_publications

//val androidRuntimeJar: Configuration by configurations.creating {
//    isCanBeConsumed = true
//    isCanBeResolved = false
//    // If you want this configuration to share the same dependencies, otherwise omit this line
//    extendsFrom(configurations["implementation"], configurations["runtimeOnly"])
//}
//
//artifacts {
//    archives(androidRuntimeJar)
//}

android.libraryVariants.forEach(::println)