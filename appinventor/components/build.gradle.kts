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
    sourceSets {
        named("main") {
            java {
                setSrcDirs(listOf("src"))
            }
        }
        named("test") {
            java {
                setSrcDirs(listOf("tests"))
            }
        }
    }
}

dependencies {
    implementation(projects.componentConstants)
    implementation(projects.barcode)
}
