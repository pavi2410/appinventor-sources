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
        "commons-io:commons-io:2.0.1",
        "org.bouncycastle:bcprov-jdk15on:1.49",
        "org.bouncycastle:bcpkix-jdk15on:1.49",
        "org.eclipse.jdt.core.compiler:ecj:4.5.1",
        "com.android.tools:common:24.3.0",
        "com.android.tools:sdk-common:24.3.0",
        "com.android.tools:sdklib:24.3.0",
        "com.android.tools.build:builder:1.3.0",
        "com.android.tools.build:builder-model:1.3.0",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)
