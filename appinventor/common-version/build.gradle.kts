import org.apache.tools.ant.filters.ReplaceTokens
import java.util.Calendar
import java.text.SimpleDateFormat

plugins {
    `java-library`
    id("org.docstr.gwt") version "1.1.21" // TODO: should apply gwt-base
}

gwt {
    gwtVersion = "2.10.0"
    modules("com.google.appinventor.common.CommonVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val taskGitBuildId by tasks.creating(Copy::class) {
    from("GitBuildId.template")
    into("$buildDir/generated/sources/java")
    rename("GitBuildId.template", "GitBuildId.java")

    filteringCharset = "UTF-8"

    filter(
        ReplaceTokens::class, mapOf(
            "tokens" to mapOf(
                "git.build.version" to execute("git describe --dirty"),
                "git.build.fingerprint" to execute("git rev-parse HEAD"),
                "ant.build.date" to getDate(),
                "acra.uri" to ""
            )
        )
    )
}

sourceSets {
    main {
        java {
            srcDirs("$buildDir/generated/sources/java")
        }
    }
}

val compileJava by tasks.existing(JavaCompile::class) {
    dependsOn(taskGitBuildId)
}

dependencies {
    implementation("com.google.guava:guava:14.0.1")
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