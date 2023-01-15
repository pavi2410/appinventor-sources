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
        #        "org.osmdroid:osmdroid-android:5.6.6",
        "org.apache.httpcomponents:httpmime:4.3.2",
        "androidx.core:core:1.0.0",
        "androidx.appcompat:appcompat:1.0.0",
        "androidx.cardview:cardview:1.0.0",
        "androidx.recyclerview:recyclerview:1.0.0",
        "ch.acra:acra:4.4.0",
        "redis.clients:jedis:3.0.0",
        "org.twitter4j:twitter4j-core:3.0.5",
        #        "com.caverock:androidsvg-aar:1.4",
        "com.firebase:firebase-client-android:2.5.0",
        "com.google.protobuf:protobuf-java:3.0.0",
        "com.google.api-client:google-api-client-android:1.30.2",
        "com.google.apis:google-api-services-sheets:v4-rev604-1.25.0",
        "com.google.apis:google-api-services-fusiontables:v1-rev19-1.13.2-beta",
        "org.json:json:20220320",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
        "https://jitpack.io",
    ],
)

android_sdk_repository(name = "androidsdk")

http_archive(
    name = "com_github_johnynek_bazel_jar_jar",
    sha256 = "e736c5249541470a9ef2b829a15d4ac314f0341ef97e2b270b8f7ecfb7171567",
    strip_prefix = "bazel_jar_jar-e6923d31e016f49345bbd1c7d83de1898796b3ea",
    url = "https://github.com/johnynek/bazel_jar_jar/archive/e6923d31e016f49345bbd1c7d83de1898796b3ea.zip",
)

load("@com_github_johnynek_bazel_jar_jar//:jar_jar.bzl", "jar_jar_repositories")

jar_jar_repositories()

http_archive(
    name = "aspect_bazel_lib",
    sha256 = "931a562afb3ea1d2c828145d8ed65b32245bfb67e4ee7d73092bae7be8dd764f",
    strip_prefix = "bazel-lib-1.19.2",
    url = "https://github.com/aspect-build/bazel-lib/archive/refs/tags/v1.19.2.tar.gz",
)

load("@aspect_bazel_lib//lib:repositories.bzl", "aspect_bazel_lib_dependencies")

aspect_bazel_lib_dependencies()

http_archive(
    name = "rules_pkg",
    sha256 = "eea0f59c28a9241156a47d7a8e32db9122f3d50b505fae0f33de6ce4d9b61834",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/releases/download/0.8.0/rules_pkg-0.8.0.tar.gz",
        "https://github.com/bazelbuild/rules_pkg/releases/download/0.8.0/rules_pkg-0.8.0.tar.gz",
    ],
)

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()
