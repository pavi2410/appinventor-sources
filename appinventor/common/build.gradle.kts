import org.apache.tools.ant.filters.ReplaceTokens
import java.util.Calendar
import java.text.SimpleDateFormat

plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val taskGitBuildId by tasks.creating(Copy::class) {
    from("GitBuildId.template")
    into("$buildDir/generated/")

    filter(
        ReplaceTokens::class, mapOf(
            "git.build.version" to execute("git describe --dirty"),
            "git.build.fingerprint" to execute("git rev-parse HEAD"),
            "ant.build.date" to getDate(),
            "acra.uri" to ""
        )
    )

    filteringCharset = "UTF-8"

    rename("GitBuildId.template", "GitBuildId.java")
}

val commonUtilsImplementation: Configuration by configurations.creating

val commonUtils: SourceSet by sourceSets.creating {
    compileClasspath += commonUtilsImplementation
    runtimeClasspath += commonUtilsImplementation
    java {
        setSrcDirs(listOf("src/com/google/appinventor/common/utils/"))
    }
    // TODO: breaks when commonUtils & commonVersion both have same resources dir
    //        resources {
    //            srcDirs = ["src/com/google/appinventor/common/"]
    //            include "CommonUtils.gwt.xml"
    //        }
}
val commonVersion: SourceSet by sourceSets.creating {
    java {
        setSrcDirs(
            listOf(
                "src/com/google/appinventor/common/version/",
                "$buildDir/generated/src/com/google/appinventor/common/version/"
            )
        )
        compiledBy(taskGitBuildId)
    }
    // TODO: see above
    //        resources {
    //            srcDirs = ["src/com/google/appinventor/common/"]
    //            include "CommonVersion.gwt.xml"
    //        }
}

repositories {
    mavenCentral()
}

dependencies {
    commonUtilsImplementation("com.google.guava:guava:14.0.1")
}

//defaultTasks("CommonUtilsJar", "CommonUtilsGwtJar", "CommonVersionJar", "CommonVersionGwtJar")

tasks {

    val taskCommonUtilsJar by creating(Jar::class) {
        from(commonUtils.allSource)

        archiveFileName.set("CommonUtils.jar")
    }

    val taskCommonUtilsGwtJar by creating(Jar::class) {
        from(commonUtils.allSource)
//        // TODO: all Java files are flattened in the JAR!
//        from(commonUtils.allJava)
        from("src/com/google/appinventor/common/CommonUtils.gwt.xml")

        archiveFileName.set("CommonUtils-gwt.jar")
    }

    val taskCommonVersionJar by creating(Jar::class) {
        from(commonVersion.allSource)

        archiveFileName.set("CommonVersion.jar")
    }

    val taskCommonVersionGwtJar by creating(Jar::class) {
        from(commonVersion.allSource)
//        // TODO: see above
//        from(commonVersion.allJava)
        from("src/com/google/appinventor/common/CommonVersion.gwt.xml")

        archiveFileName.set("CommonVersion-gwt.jar")
    }

    artifacts {
        archives(taskCommonUtilsJar)
        archives(taskCommonUtilsGwtJar)
        archives(taskCommonVersionJar)
        archives(taskCommonVersionGwtJar)
    }
}

fun execute(command: String): String {
    val process = Runtime.getRuntime().exec(command)
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()
    return output.trim()
}

fun getDate(): String {
    val date = Calendar.getInstance().time
    val formatter = SimpleDateFormat("MMMM dd yyyy")
    return formatter.format(date)
}