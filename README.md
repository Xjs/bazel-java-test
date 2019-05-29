# Hermeticity violations with Bazel and Java

One of [Bazel](https://bazel.build)'s unique strengths is _hermeticity_, or the ability to create reproducible builds. Ideally, build artefacts created from the same input files are bit-by-bit identical.

In this repository, a couple of ways are outlined in which this assumption is violated under changes of environmental conditions, such as the build execution platform or the JDK installed on the build host. This repository concentrates solely on Java builds. The artifact in question will always be the `main_deploy.jar` target which is [implicitly created by the java_binary](https://docs.bazel.build/versions/0.25.0/be/java.html#java_binary_implicit_outputs) rule.

This repository serves as a supplement to corresponding [github.com/bazelbuild/bazel](github.com/bazelbuild/bazel) bug reports.

## Case 1: Platform name and line endings in `build-data.properties` (the obvious case)

`main_deploy.jar` contains a file called `build-data.properties`. When built on a Windows host, this file will contain some `\r` characters, which on Windows accompany `\n` as line feed character. (Curiously, only the three lines containing a time in various formats will end in `\r\n`.) Additionally, the file will contain a string identifying the build host system.

[Read more...](case1.md)

## Case 2: Different class binaries with JDK 8 and JDK 11

This repository contains a minimal example with some files from the [Google protobuf library](https://developers.google.com/protocol-buffers/). _(Please see the source files [ByteBufferWriter.java](src/bazeltest/ByteBufferWriter) and [UnsafeUtil.java](src/bazeltest/UnsafeUtil.java) for redistribution terms.)_ If the host JDK that is used to execute Bazel is JDK 8, the binary `ByteBufferWriter.class` is slightly different than when JDK 11 is used.

[Read more...](case2.md)

## Case 3: Line endings (again!) in genrules

When a genrule from an external git repository is invoked on Windows, some system-wide `config.autocrlf` git configuration option is used. This may lead to extra `\r` characters in input files which are not present in the repository itself. These may be reproduced in the generated files, leading to differences in the generated files.

[Read more...](case3.md)
