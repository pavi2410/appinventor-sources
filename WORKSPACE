load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "bazel_skylib",
    sha256 = "74d544d96f4a5bb630d465ca8bbcfe231e3594e5aae57e1edbf17a6eb3ca2506",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/1.3.0/bazel-skylib-1.3.0.tar.gz",
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.3.0/bazel-skylib-1.3.0.tar.gz",
    ],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "4.5"

RULES_JVM_EXTERNAL_SHA = "b17d7388feb9bfa7f2fa09031b32707df529f26c91ab9e5d909eb1676badd9a6"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "org.json:json:20220320",
        "args4j:args4j:2.0.22",
        "com.google.guava:guava:14.0.1",
        #        "com.google.guava:guava:27.0.1-android",
        "commons-io:commons-io:2.0.1",
        "org.bouncycastle:bcprov-jdk15on:1.49",
        "org.bouncycastle:bcpkix-jdk15on:1.49",
        "org.eclipse.jdt.core.compiler:ecj:4.5.1",
        "com.android.tools:common:24.3.0",
        "com.android.tools:sdk-common:24.3.0",
        "com.android.tools:sdklib:24.3.0",
        "com.android.tools.build:builder:1.3.0",
        "com.android.tools.build:builder-model:1.3.0",
        "com.sun.grizzly:grizzly-http:1.9.18-i",
        "com.sun.jersey:jersey-bundle:1.3",
        "org.codehaus.jettison:jettison:1.1",
        "com.google.zxing:core:2.1",
        "com.github.PhilJay:MPAndroidChart:v3.1.0",
        "org.locationtech.jts:jts-core:1.15.0",
        "org.osmdroid:osmdroid-android:6.1.11",
        "org.apache.httpcomponents:httpmime:4.3.2",
        "androidx.core:core:1.0.0",
        "androidx.appcompat:appcompat:1.0.0",
        "androidx.cardview:cardview:1.0.0",
        "androidx.recyclerview:recyclerview:1.0.0",
        "ch.acra:acra:4.4.0",
        "redis.clients:jedis:3.0.0",
        "org.twitter4j:twitter4j-core:3.0.5",
        "com.caverock:androidsvg-aar:1.4",
        "com.firebase:firebase-client-android:2.5.0",
        "com.google.protobuf:protobuf-java:3.0.0",
        "com.google.api-client:google-api-client-android:1.30.2",
        "com.google.apis:google-api-services-sheets:v4-rev604-1.25.0",
        "com.google.apis:google-api-services-fusiontables:v1-rev19-1.13.2-beta",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
        "https://jitpack.io",
    ],
)

android_sdk_repository(name = "androidsdk")
