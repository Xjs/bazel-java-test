# Hermeticity violations with Bazel and Java

One of [Bazel](https://bazel.build)'s unique strengths is _hermeticity_, or the ability to create reproducible builds. Ideally, build artefacts created from the same input files are bit-by-bit identical.

In this repository, a couple of ways are outlined in which this assumption is violated under changes of environmental conditions, such as the build execution platform or the JDK installed on the build host. This repository concentrates solely on Java builds. The artifact in question will always be the `main_deploy.jar` target which is [implicitly created by the java_binary](https://docs.bazel.build/versions/0.25.0/be/java.html#java_binary_implicit_outputs) rule.

This repository serves as a supplement to corresponding [github.com/bazelbuild/bazel](github.com/bazelbuild/bazel) bug reports.

## Case 2: Different class binaries with JDK 8 and JDK 11

This repository contains a minimal example with some files from the [Google protobuf library](https://developers.google.com/protocol-buffers/). _(Please see the source files [ByteBufferWriter.java](src/bazeltest/ByteBufferWriter) and [UnsafeUtil.java](src/bazeltest/UnsafeUtil.java) for redistribution terms.)_ If the host JDK that is used to execute Bazel is JDK 8, the binary `ByteBufferWriter.class` is slightly different than when JDK 11 is used.

## Steps to reproduce

On a system with JDK 8 installed, run:
```
$ bazel build main_deploy.jar
$ mkdir -p main_deploy && cp bazel-bin/main_deploy.jar main_deploy
$ cd main_deploy
$ jar -xf main_deploy.jar && rm -f main_deploy.jar
```

Observe the file `bazeltest/ByteBufferWriter.class` having length 4226:

```
$ wc bazeltest/ByteBufferWriter.class
  53  100 4226 main_deploy/bazeltest/ByteBufferWriter.class
```

Java version used for testing:
```
$ java -version
openjdk version "1.8.0_191-1-redhat"
OpenJDK Runtime Environment (build 1.8.0_191-1-redhat-b12)
OpenJDK 64-Bit Server VM (build 25.191-b12, mixed mode)
```

On a system with JDK 11 installed, run the same commands:
```
$ bazel build main_deploy.jar
$ mkdir -p main_deploy && cp bazel-bin/main_deploy.jar main_deploy
$ cd main_deploy
$ jar -xf main_deploy.jar && rm -f main_deploy.jar
```

Observe the file `bazeltest/ByteBufferWriter.class` having length 4230:

```
$ wc bazeltest/ByteBufferWriter.class
  53  100 4230 bazeltest/ByteBufferWriter.class
```

Java version used for testing:

```
$ java -version
openjdk version "11.0.3" 2019-04-16
OpenJDK Runtime Environment (build 11.0.3+7-Ubuntu-1ubuntu218.04.1)
OpenJDK 64-Bit Server VM (build 11.0.3+7-Ubuntu-1ubuntu218.04.1, mixed mode, sharing)
```

## Possible leads

Inspection with `hexdump` reveals that the sole difference is the string `java/nio/Buffer` (JDK11: `java/nio/ByteBuffer`) plus its length around bytes 2224-2240 (-ish). This leads to the suspicion that some internal implementation detail in the [`java.nio.ByteBuffer`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/ByteBuffer.html) class ([JDK 8 doc](https://docs.oracle.com/javase/8/docs/api/java/nio/ByteBuffer.html)) has changed. I have not investigated much further yet.
