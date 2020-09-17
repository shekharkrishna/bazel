load("@rules_java//java:defs.bzl", "java_import", "java_runtime", "java_toolchain")
load(
    "//tools/jdk:default_java_toolchain.bzl",
    "JDK8_JVM_OPTS",
    "bootclasspath",
    "default_java_toolchain",
    "java_runtime_files",
)
load(
    "//tools/jdk:java_toolchain_alias.bzl",
    "java_host_runtime_alias",
    "java_runtime_alias",
    "java_toolchain_alias",
    "legacy_java_runtime_alias",
    "legacy_java_toolchain_alias",
)
load(
    "//tools/jdk:remote_java_tools_aliases.bzl",
    "remote_java_tools_filegroup",
    "remote_java_tools_java_import",
)
load("@rules_cc//cc:defs.bzl", "cc_binary", "cc_library")
load("//tools/python:private/defs.bzl", "py_binary", "py_test")

package(default_visibility = ["//visibility:public"])

# Used to distinguish toolchains used for Java development, ie the JavaToolchainProvider.
toolchain_type(name = "toolchain_type")

# Used to distinguish toolchains used for Java execution, ie the JavaRuntimeInfo.
toolchain_type(name = "runtime_toolchain_type")

# Points to either :runtime_toolchain_type or :legacy_current_java_runtime
java_runtime_alias(name = "current_java_runtime")

# Points to :current_java_runtime with host transition
java_host_runtime_alias(name = "current_host_java_runtime")

# Points to either :toolchain_type or :legacy_current_java_toolchain
java_toolchain_alias(name = "current_java_toolchain")

# This exists to support the migration to toolchain resolution.
# TODO(cushon): delete once the migration is complete.
# Points to --javabase/--host_javabase, default value ":jdk"/":remote_jdk11"
legacy_java_runtime_alias(name = "legacy_current_java_runtime")

# Points to --java_toolchain/--host_java_toolchain, default value ":toolchain"/":remote_toolchain"
legacy_java_toolchain_alias(name = "legacy_current_java_toolchain")

# Used to set --host_javabase or --javabase to a local JDK without having to define
# a custom java_runtime rule.
# E.g.:
# bazel build --host_javabase=@bazel_tools//tools/jdk:absolute_javabase \
#   --define=ABSOLUTE_JAVABASE=<path to JDK> ...
java_runtime(
    name = "absolute_javabase",
    java_home = "$(ABSOLUTE_JAVABASE)",
)

filegroup(
    name = "BUILD-jdk",
    srcs = [":BUILD"],
)

# These individual jni_* targets are exposed for legacy reasons.
# Most users should depend on :jni.

java_runtime_files(
    name = "jni_header",
    srcs = ["include/jni.h"],
)

java_runtime_files(
    name = "jni_md_header-darwin",
    srcs = ["include/darwin/jni_md.h"],
)

java_runtime_files(
    name = "jni_md_header-linux",
    srcs = ["include/linux/jni_md.h"],
)

java_runtime_files(
    name = "jni_md_header-windows",
    srcs = ["include/win32/jni_md.h"],
)

java_runtime_files(
    name = "jni_md_header-freebsd",
    srcs = ["include/freebsd/jni_md.h"],
)

java_runtime_files(
    name = "jni_md_header-openbsd",
    srcs = ["include/openbsd/jni_md.h"],
)

# The Java native interface. Depend on this package if you #include <jni.h>.
#
# See test_jni in third_party/bazel/src/test/shell/bazel/bazel_java_test.sh for
# an example of using Bazel to build a Java program that calls a C function.
#
# TODO(adonovan): add cases for //src/conditions:linux_arm when released in Bazel.
# TODO(adonovan): why is there no //src/conditions target meaning "just linux"?
# Required by: "//src/main/native..."
cc_library(
    name = "jni",
    hdrs = [":jni_header"] + select({
        "//src/conditions:linux_aarch64": [":jni_md_header-linux"],
        "//src/conditions:linux_ppc64le": [":jni_md_header-linux"],
        "//src/conditions:linux_s390x": [":jni_md_header-linux"],
        "//src/conditions:linux_x86_64": [":jni_md_header-linux"],
        "//src/conditions:darwin": [":jni_md_header-darwin"],
        "//src/conditions:freebsd": [":jni_md_header-freebsd"],
        "//src/conditions:openbsd": [":jni_md_header-openbsd"],
        "//src/conditions:windows": [":jni_md_header-windows"],
        "//conditions:default": [],
    }),
    includes = ["include"] + select({
        "//src/conditions:linux_aarch64": ["include/linux"],
        "//src/conditions:linux_ppc64le": ["include/linux"],
        "//src/conditions:linux_s390x": ["include/linux"],
        "//src/conditions:linux_x86_64": ["include/linux"],
        "//src/conditions:darwin": ["include/darwin"],
        "//src/conditions:freebsd": ["include/freebsd"],
        "//src/conditions:openbsd": ["include/openbsd"],
        "//src/conditions:windows": ["include/win32"],
        "//conditions:default": [],
    }),
)

# On Windows, executables end in ".exe", but the label we reach it through
# must be platform-independent. Thus, we create a little filegroup that
# contains the appropriate platform-dependent file.

# Required by: Android, some tests
remote_java_tools_filegroup(
    name = "ijar",
    target = ":ijar",
)

# On Windows, Java implementation of singlejar is used. We create a little
# filegroup that contains the appropriate platform-dependent file.
# Once https://github.com/bazelbuild/bazel/issues/2241 is fixed (that is,
# the native singlejar is used on windows), this file group can be reused since
# on Windows, executables end in ".exe", but the label we reach it through
# must be platform-independent.
# Required by: Android, scala
remote_java_tools_filegroup(
    name = "singlejar",
    target = ":singlejar",
)

exports_files(["BUILD.java_tools"])

# Required by: Bazel java_test, java_binary rule
remote_java_tools_java_import(
    name = "JacocoCoverageRunner",
    target = ":java_tools/JacocoCoverage_jarjar_deploy.jar",
)

# Required by: Bazel java_test rule
remote_java_tools_java_import(
    name = "TestRunner",
    target = ":java_tools/Runner_deploy.jar",
)

# Required by: Android, scala
remote_java_tools_filegroup(
    name = "TestRunner_deploy.jar",
    target = ":java_tools/Runner_deploy.jar",
)

# Required by: Android: @bazel_tools//tools/android:build_java8_legacy_dex
alias(
    name = "proguard",
    actual = select({
        "//src/conditions:darwin": "@remote_java_tools_darwin//:proguard",
        "//src/conditions:darwin_x86_64": "@remote_java_tools_darwin//:proguard",
        "//src/conditions:windows": "@remote_java_tools_windows//:proguard",
        "//src/conditions:linux_x86_64": "@remote_java_tools_linux//:proguard",
        # On different platforms the linux repository can be used because the
        # proguard deploy jar is platform-agnostic.
        "//conditions:default": "@remote_java_tools_linux//:proguard",
    }),
)

filegroup(
    name = "langtools",
    srcs = ["//third_party/java/jdk/langtools:javac_jar"],
)

java_import(
    name = "langtools-neverlink",
    jars = [":langtools"],
    neverlink = 1,
)

# Required by: legacy targets
alias(
    name = "jdk",
    actual = "@local_jdk//:jdk",
)

# TODO cleaner definition using toolchains
bootclasspath(
    name = "platformclasspath",
    src = "DumpPlatformClassPath.java",
    host_javabase = ":current_java_runtime",
    target_javabase = ":current_java_runtime",
)

# Required by: legacy targets
alias(
    name = "toolchain",
    actual = select({
        "//src/conditions:darwin": "@remote_java_tools_darwin//:toolchain",
        "//src/conditions:darwin_x86_64": "@remote_java_tools_darwin//:toolchain",
        "//src/conditions:windows": "@remote_java_tools_windows//:toolchain",
        "//src/conditions:linux_x86_64": "@remote_java_tools_linux//:toolchain",
        "//conditions:default": "@bazel_tools//tools/jdk:legacy_toolchain",
    }),
)

# Required by: legacy targets
alias(
    name = "remote_toolchain",
    actual = select({
        "//src/conditions:darwin": "@remote_java_tools_darwin//:toolchain",
        "//src/conditions:darwin_x86_64": "@remote_java_tools_darwin//:toolchain",
        "//src/conditions:windows": "@remote_java_tools_windows//:toolchain",
        "//src/conditions:linux_x86_64": "@remote_java_tools_linux//:toolchain",
        "//conditions:default": "@bazel_tools//tools/jdk:legacy_toolchain",
    }),
)

filegroup(
    name = "srcs",
    srcs = [
        "BUILD.java_langtools",
        "BUILD.java_tools",
        "BUILD-jdk",  # Tools are build from the workspace for tests.
        "DumpPlatformClassPath.java",
        "default_java_toolchain.bzl",
        "fail_rule.bzl",
        "java_toolchain_alias.bzl",
        "jdk.BUILD",
        "local_java_repository.bzl",
        "nosystemjdk/README",
        "proguard_whitelister.py",
        "proguard_whitelister_test.py",
        "proguard_whitelister_test_input.pgcfg",
        "remote_java_tools_aliases.bzl",
        "toolchain_utils.bzl",
    ],
)

filegroup(
    name = "bzl_srcs",
    srcs = glob(["*.bzl"]),
    visibility = ["//tools:__pkg__"],
)

filegroup(
    name = "package-srcs",
    srcs = glob(["**"]),
)

py_binary(
    name = "proguard_whitelister",
    srcs = [
        "proguard_whitelister.py",
    ],
    deps = [
        "//third_party/py/abseil",
    ],
)

py_test(
    name = "proguard_whitelister_test",
    srcs = ["proguard_whitelister_test.py"],
    data = ["proguard_whitelister_test_input.pgcfg"],
    deps = [
        ":proguard_whitelister",
    ],
)

test_suite(
    name = "windows_tests",
    tags = [
        "-no_windows",
        "-slow",
    ],
    visibility = ["//visibility:private"],
)

test_suite(
    name = "all_windows_tests",
    tests = [":windows_tests"],
    visibility = ["//tools:__pkg__"],
)

# A JDK 11 for use as a --host_javabase.
# Required by: legacy targets
alias(
    name = "remote_jdk11",
    actual = select(
        {
            "//src/conditions:darwin": "@remotejdk11_macos//:jdk",
            "//src/conditions:darwin_x86_64": "@remotejdk11_macos//:jdk",
            "//src/conditions:windows": "@remotejdk11_win//:jdk",
            "//src/conditions:linux_aarch64": "@remotejdk11_linux_aarch64//:jdk",
            "//src/conditions:linux_x86_64": "@remotejdk11_linux//:jdk",
            "//src/conditions:linux_ppc64le": "@remotejdk11_linux_ppc64le//:jdk",
            "//src/conditions:linux_s390x": "@remotejdk11_linux_s390x//:jdk",
        },
        no_match_error = "Could not find a JDK for host execution environment, please explicitly" +
                         " provide one using `--host_javabase.`",
    ),
    visibility = ["//visibility:public"],
)

# Java toolchains

java_toolchain(
    name = "toolchain_linux",
    bootclasspath = ["@bazel_tools//tools/jdk:platformclasspath"],
    forcibly_disable_header_compilation = 0,
    genclass = ["@remote_java_tools_linux//:GenClass"],
    header_compiler = ["@remote_java_tools_linux//:Turbine"],
    header_compiler_direct = ["@remote_java_tools_linux//:TurbineDirect"],
    ijar = ["@remote_java_tools_linux//:ijar"],  # TODO has selects
    jacocorunner = "@remote_java_tools_linux//:jacoco_coverage_runner_filegroup",
    javabuilder = ["@remote_java_tools_linux//:JavaBuilder"],
    javac = ["@remote_java_tools_linux//:javac_jar"],
    javac_supports_workers = 1,
    jvm_opts = [
        # In JDK9 we have seen a ~30% slow down in JavaBuilder performance
        # when using G1 collector and having compact strings enabled.
        "-XX:+UseParallelOldGC",
        "-XX:-CompactStrings",
        # Allow JavaBuilder to access internal javac APIs.
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",

        # override the javac in the JDK.
        "--patch-module=java.compiler=$(location @remote_java_tools_linux//:java_compiler_jar)",
        "--patch-module=jdk.compiler=$(location @remote_java_tools_linux//:jdk_compiler_jar)",

        # quiet warnings from com.google.protobuf.UnsafeUtil,
        # see: https://github.com/google/protobuf/issues/3781
        # and: https://github.com/bazelbuild/bazel/issues/5599
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
    ],
    misc = [
        "-XDskipDuplicateBridges=true",
        "-g",
        "-parameters",
    ],
    singlejar = ["@remote_java_tools_linux//:singlejar"],  # TODO has selects
    source_version = "8",
    target_version = "8",
    tools = [
        "@remote_java_tools_linux//:java_compiler_jar",
        "@remote_java_tools_linux//:jdk_compiler_jar",
    ],
)

java_toolchain(
    name = "toolchain_openbsd",
    bootclasspath = ["@bazel_tools//tools/jdk:platformclasspath"],
    forcibly_disable_header_compilation = 0,
    genclass = ["@remote_java_tools_linux//:GenClass"],
    header_compiler = ["@remote_java_tools_linux//:Turbine"],
    header_compiler_direct = ["@remote_java_tools_linux//:TurbineDirect"],
    ijar = ["@remote_java_tools_linux//:ijar"],  # TODO has selects
    jacocorunner = "@remote_java_tools_linux//:jacoco_coverage_runner_filegroup",
    javabuilder = ["@remote_java_tools_linux//:JavaBuilder"],
    javac = ["@remote_java_tools_linux//:javac_jar"],
    javac_supports_workers = 1,
    jvm_opts = [
        "-Xbootclasspath/p:$(location @remote_java_tools_linux//:javac_jar)",
    ],
    misc = [
        "-XDskipDuplicateBridges=true",
        "-g",
        "-parameters",
    ],
    singlejar = ["@remote_java_tools_linux//:singlejar"],  # TODO has select
    source_version = "8",
    target_version = "8",
    tools = [
        "@remote_java_tools_linux//:java_compiler_jar",
        "@remote_java_tools_linux//:jdk_compiler_jar",
    ],
)

java_toolchain(
    name = "toolchain_windows",
    bootclasspath = ["@bazel_tools//tools/jdk:platformclasspath"],
    forcibly_disable_header_compilation = 0,
    genclass = ["@remote_java_tools_windows//:GenClass"],
    header_compiler = ["@remote_java_tools_windows//:Turbine"],
    header_compiler_direct = ["@remote_java_tools_windows//:TurbineDirect"],
    ijar = ["@remote_java_tools_windows//:ijar"],  # TODO has selects
    jacocorunner = "@remote_java_tools_windows//:jacoco_coverage_runner_filegroup",
    javabuilder = ["@remote_java_tools_windows//:JavaBuilder"],
    javac = ["@remote_java_tools_windows//:javac_jar"],
    javac_supports_workers = 1,
    jvm_opts = [
        # In JDK9 we have seen a ~30% slow down in JavaBuilder performance when using
        # G1 collector and having compact strings enabled.
        "-XX:+UseParallelOldGC",
        "-XX:-CompactStrings",
        # Allow JavaBuilder to access internal javac APIs.
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",

        # override the javac in the JDK.
        "--patch-module=java.compiler=$(location @remote_java_tools_windows//:java_compiler_jar)",
        "--patch-module=jdk.compiler=$(location @remote_java_tools_windows//:jdk_compiler_jar)",

        # quiet warnings from com.google.protobuf.UnsafeUtil,
        # see: https://github.com/google/protobuf/issues/3781
        # and: https://github.com/bazelbuild/bazel/issues/5599
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
    ],
    misc = [
        "-XDskipDuplicateBridges=true",
        "-g",
        "-parameters",
    ],
    singlejar = ["@remote_java_tools_windows//:singlejar"],  # TODO has selects
    source_version = "8",
    target_version = "8",
    tools = [
        "@remote_java_tools_windows//:java_compiler_jar",
        "@remote_java_tools_windows//:jdk_compiler_jar",
    ],
)

java_toolchain(
    name = "toolchain_darwin",
    bootclasspath = ["@bazel_tools//tools/jdk:platformclasspath"],
    forcibly_disable_header_compilation = 0,
    genclass = ["@remote_java_tools_darwin//:GenClass"],
    header_compiler = ["@remote_java_tools_darwin//:Turbine"],
    header_compiler_direct = ["@remote_java_tools_darwin//:TurbineDirect"],
    ijar = ["@remote_java_tools_darwin//:ijar"],  # TODO has selects
    jacocorunner = "@remote_java_tools_darwin//:jacoco_coverage_runner_filegroup",
    javabuilder = ["@remote_java_tools_darwin//:JavaBuilder"],
    javac = ["@remote_java_tools_darwin//:javac_jar"],
    javac_supports_workers = 1,
    jvm_opts = [
        # In JDK9 we have seen a ~30% slow down in JavaBuilder performance when using
        # G1 collector and having compact strings enabled.
        "-XX:+UseParallelOldGC",
        "-XX:-CompactStrings",
        # Allow JavaBuilder to access internal javac APIs.
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",

        # override the javac in the JDK.
        "--patch-module=java.compiler=$(location @remote_java_tools_darwin//:java_compiler_jar)",
        "--patch-module=jdk.compiler=$(location @remote_java_tools_darwin//:jdk_compiler_jar)",

        # quiet warnings from com.google.protobuf.UnsafeUtil,
        # see: https://github.com/google/protobuf/issues/3781
        # and: https://github.com/bazelbuild/bazel/issues/5599
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
    ],
    misc = [
        "-XDskipDuplicateBridges=true",
        "-g",
        "-parameters",
    ],
    singlejar = ["@remote_java_tools_darwin//:singlejar"],  # TODO has selects
    source_version = "8",
    target_version = "8",
    tools = [
        "@remote_java_tools_darwin//:java_compiler_jar",
        "@remote_java_tools_darwin//:jdk_compiler_jar",
    ],
)

toolchain(
    name = "remote_java_tools_linux",
    exec_compatible_with = [
        "@platforms//os:linux",
        "@platforms//cpu:x86_64",
    ],
    toolchain = ":toolchain_linux",
    toolchain_type = ":toolchain_type",
)

toolchain(
    name = "remote_java_tools_freebsd",
    exec_compatible_with = [
        "@platforms//os:freebsd",
        "@platforms//cpu:x86_64",
    ],
    toolchain = ":toolchain_linux",
    toolchain_type = ":toolchain_type",
)

toolchain(
    name = "remote_java_tools_openbsd",
    exec_compatible_with = [
        "@platforms//os:openbsd",
        "@platforms//cpu:x86_64",
    ],
    toolchain = ":toolchain_linux",
    toolchain_type = ":toolchain_type",
)

toolchain(
    name = "remote_java_tools_windows",
    exec_compatible_with = [
        "@platforms//os:windows",
        "@platforms//cpu:x86_64",
    ],
    toolchain = ":toolchain_windows",
    toolchain_type = ":toolchain_type",
)

toolchain(
    name = "remote_java_tools_darwin",
    exec_compatible_with = [
        "@platforms//os:macos",
        "@platforms//cpu:x86_64",
    ],
    toolchain = ":toolchain_darwin",
    toolchain_type = ":toolchain_type",
)

# Java Runtime toolchains

load("@local_config_platform//:constraints.bzl", "HOST_CONSTRAINTS")

toolchain(
    name = "local_jdk",
    exec_compatible_with = HOST_CONSTRAINTS,
    toolchain = "@local_jdk//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)

toolchain(
    name = "remotejdk11_linux",
    exec_compatible_with = [
        "@platforms//os:linux",
        "@platforms//cpu:x86_64",
    ],
    toolchain = "@remotejdk11_linux//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)

toolchain(
    name = "remotejdk11_linux_aarch64",
    exec_compatible_with = [
        "@platforms//os:linux",
        "@platforms//cpu:aarch64",
    ],
    toolchain = "@remotejdk11_linux_aarch64//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)

toolchain(
    name = "remotejdk11_linux_ppc64le",
    exec_compatible_with = [
        "@platforms//os:linux",
        "@platforms//cpu:ppc",
    ],
    toolchain = "@remotejdk11_linux_ppc64le//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)

toolchain(
    name = "remotejdk11_linux_s390x",
    exec_compatible_with = [
        "@platforms//os:linux",
        "@platforms//cpu:s390x",
    ],
    toolchain = "@remotejdk11_linux_s390x//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)

toolchain(
    name = "remotejdk11_macos",
    exec_compatible_with = [
        "@platforms//os:macos",
        "@platforms//cpu:x86_64",
    ],
    toolchain = "@remotejdk11_macos//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)

toolchain(
    name = "remotejdk11_win",
    exec_compatible_with = [
        "@platforms//os:windows",
        "@platforms//cpu:x86_64",
    ],
    toolchain = "@remotejdk11_win//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)

toolchain(
    name = "remotejdk14_linux",
    exec_compatible_with = [
        "@platforms//os:linux",
        "@platforms//cpu:x86_64",
    ],
    toolchain = "@remotejdk14_linux//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)

toolchain(
    name = "remotejdk14_macos",
    exec_compatible_with = [
        "@platforms//os:macos",
        "@platforms//cpu:x86_64",
    ],
    toolchain = "@remotejdk14_macos//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)

toolchain(
    name = "remotejdk14_win",
    exec_compatible_with = [
        "@platforms//os:windows",
        "@platforms//cpu:x86_64",
    ],
    toolchain = "@remotejdk14_win//:jdk",
    toolchain_type = ":runtime_toolchain_type",
)
