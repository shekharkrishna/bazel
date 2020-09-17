def define_toolchain_aliases():
    # This is necessary to get the *host* Java runtime. Depending on
    # //tools/jdk:current_java_runtime from an attribute with the host transition
    # does not work because the dependency is determined based on the configuration
    # *before* the transition.
    alias(
        name = "java_runtime_alias",
        actual = "@bazel_tools//tools/jdk:current_java_runtime",
    )
    alias(
        name = "java",
        actual = "@local_jdk//:java",
    )

    alias(
        name = "jar",
        actual = "@local_jdk//:jar",
    )

    alias(
        name = "javac",
        actual = "@local_jdk//:javac",
    )

    alias(
        name = "javadoc",
        actual = "@local_jdk//:javadoc",
    )

    remote_java_tools_filegroup(
        name = "GenClass_deploy.jar",
        target = ":java_tools/GenClass_deploy.jar",
    )

    remote_java_tools_filegroup(
        name = "bazel-singlejar_deploy.jar",
        target = ":java_tools/bazel-singlejar_deploy.jar",
    )

    remote_java_tools_filegroup(
        name = "turbine_deploy.jar",
        target = ":java_tools/turbine_deploy.jar",
    )

    remote_java_tools_filegroup(
        name = "turbine_direct_binary_deploy.jar",
        target = ":java_tools/turbine_direct_binary_deploy.jar",
    )

    remote_java_tools_filegroup(
        name = "JavaBuilder_deploy.jar",
        target = ":java_tools/JavaBuilder_deploy.jar",
    )

    # TODO(cushon): this isn't compatible with JDK 9
    alias(
        name = "bootclasspath",
        actual = "@local_jdk//:bootclasspath",
    )

    alias(
        name = "extclasspath",
        actual = "@local_jdk//:extdir",
    )

    # TODO(cushon): migrate to extclasspath and delete
    alias(
        name = "extdir",
        actual = "@local_jdk//:extdir",
    )

    alias(
        name = "jre",
        actual = "@local_jdk//:jre",
    )

    alias(
        name = "host_jdk",
        actual = ":remote_jdk11",
    )

    default_java_toolchain(
        name = "toolchain_hostjdk8",
        jvm_opts = JDK8_JVM_OPTS,
        source_version = "8",
        target_version = "8",
    )

    # Default to the Java 8 language level.
    # TODO(cushon): consider if/when we should increment this?
    default_java_toolchain(
        name = "legacy_toolchain",
        source_version = "8",
        target_version = "8",
    )

    # The 'vanilla' toolchain is an unsupported alternative to the default.
    #
    # It does not provider any of the following features:
    #   * Error Prone
    #   * Strict Java Deps
    #   * Header Compilation
    #   * Reduced Classpath Optimization
    #
    # It uses the version of javac from the `--host_javabase` instead of the
    # embedded javac, which may not be source- or bug-compatible with the embedded
    # javac.
    #
    # However it does allow using a wider range of `--host_javabase`s, including
    # versions newer than the current embedded JDK.
    default_java_toolchain(
        name = "toolchain_vanilla",
        forcibly_disable_header_compilation = True,
        javabuilder = [":vanillajavabuilder"],
        jvm_opts = [],
        source_version = "",
        target_version = "",
    )

    RELEASES = (8, 9, 10, 11)

    [
        default_java_toolchain(
            name = "toolchain_java%d" % release,
            source_version = "%s" % release,
            target_version = "%s" % release,
        )
        for release in RELEASES
    ]

    remote_java_tools_java_import(
        name = "JacocoCoverage",
        target = ":java_tools/JacocoCoverage_jarjar_deploy.jar",
    )
    remote_java_tools_java_import(
        name = "JarJar",
        target = ":JarJar",
    )
