import org.apache.tools.ant.filters.ReplaceTokens
import java.util.Calendar
import java.text.SimpleDateFormat

plugins {
    id("org.docstr.gwt") version "1.1.21"
}

gwt {
    gwtVersion = "2.8.1"
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