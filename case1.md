# Hermeticity violations with Bazel and Java

One of [Bazel](https://bazel.build)'s unique strengths is /hermeticity/, or the ability to create reproducible builds. Ideally, build artefacts created from the same input files are bit-by-bit identical.

In this repository, a couple of ways are outlined in which this assumption is violated under changes of environmental conditions, such as the build execution platform or the JDK installed on the build host. This repository concentrates solely on Java builds. The artifact in question will always be the `main_deploy.jar` target which is [implicitly created by the java_binary](https://docs.bazel.build/versions/0.25.0/be/java.html#java_binary_implicit_outputs) rule.

This repository serves as a supplement to corresponding [github.com/bazelbuild/bazel](github.com/bazelbuild/bazel) bug reports.

## Case 1: Platform name and line endings in `build-data.properties` (the obvious case)

`main_deploy.jar` contains a file called `build-data.properties`. When built on a Windows host, this file will contain some `\r` characters, which on Windows accompany `\n` as line feed character. (Curiously, only the three lines containing a time in various formats will end in `\r\n`.) Additionally, the file will contain a string identifying the build host system.

## Steps to reproduce

On a UNIX system, run:
```
$ bazel build main_deploy.jar
$ mkdir -p main_deploy && cp bazel-bin/main_deploy.jar main_deploy
$ cd main_deploy
$ jar -xf main_deploy.jar && rm -f main_deploy.jar
```

Observe the file `build-data.properties` containing approximately the following (line endings emphasized for clarity):

```
build.target=bazel-out/k8-fastbuild/bin/main_deploy.jar\n
main.class=src.bazeltest.Main\n
build.timestamp.as.int=0\n
build.timestamp=Thu Jan 01 00\:00\:00 1970 (0)\n
build.time=Thu Jan 01 00\:00\:00 1970 (0)\n
```

On a Windows system, run the same commands:
```
$ bazel build main_deploy.jar
$ mkdir -p main_deploy && cp bazel-bin/main_deploy.jar main_deploy
$ cd main_deploy
$ jar -xf main_deploy.jar && rm -f main_deploy.jar
```
Observe the file `build-data.properties` containing approximately the following (line endings again emphasized for clarity):
```
build.target=bazel-out/x64_windows-fastbuild/bin/main_deploy.jar\n
main.class=src.bazeltest.Main\n
build.timestamp.as.int=0\r\n
build.timestamp=Thu Jan 01 00\:00\:00 1970 (0)\r\n
build.time=Thu Jan 01 00\:00\:00 1970 (0)\r\n
```
