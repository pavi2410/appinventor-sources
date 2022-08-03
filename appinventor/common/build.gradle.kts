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

val commonUtils = sourceSets.creating {
    compileClasspath += sourceSets.getByName("main").output
    runtimeClasspath += sourceSets.getByName("main").output
    java {
        setSrcDirs(listOf("src/com/google/appinventor/common/utils/"))
    }
// TODO: breaks when commonUtils & commonVersion both have same resources dir
//        resources {
//            srcDirs = ["src/com/google/appinventor/common/"]
//            include "CommonUtils.gwt.xml"
//        }
}
val commonVersion = sourceSets.creating {
    java {
        setSrcDirs(
            listOf(
                "src/com/google/appinventor/common/version/",
                "$buildDir/generated/src/com/google/appinventor/common/version/"
            )
        )
    }
// TODO: see above
//        resources {
//            srcDirs = ["src/com/google/appinventor/common/"]
//            include "CommonVersion.gwt.xml"
//        }
}

//val commonUtilsImplementation by configurations

dependencies {
    implementation("com.google.guava:guava:14.0.1")
}

defaultTasks("CommonUtilsJar", "CommonUtilsGwtJar", "CommonVersionJar", "CommonVersionGwtJar")

val taskGitBuildId = tasks.registering(Copy::class) {
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

val compileJava by tasks.existing(JavaCompile::class)
compileJava.get().dependsOn(taskGitBuildId)

tasks.register<Jar>("CommonUtilsJar") {
    from(commonUtils)

    archiveFileName.set("CommonUtils-gwt.jar")
}

tasks.register<Jar>("CommonUtilsGwtJar") {
    from(commonUtils)
    // TODO: all Java files are flattened in the JAR!
//    from(commonUtils.allJava)
    from("src/com/google/appinventor/common/CommonUtils.gwt.xml")

    archiveFileName.set("CommonUtils-gwt.jar")
}

tasks.register<Jar>("CommonVersionJar") {
    from(commonVersion)

    archiveFileName.set("CommonVersion.jar")
}

tasks.register<Jar>("CommonVersionGwtJar") {
    from(commonVersion)
    // TODO: see above
//    from(commonVersion.allJava)
    from("src/com/google/appinventor/common/CommonVersion.gwt.xml")

    archiveFileName.set("CommonVersion-gwt.jar")
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