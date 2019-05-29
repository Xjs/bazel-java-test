# Hermeticity violations with Bazel and Java

One of [Bazel](https://bazel.build)'s unique strengths is _hermeticity_, or the ability to create reproducible builds. Ideally, build artefacts created from the same input files are bit-by-bit identical.

In this repository, a couple of ways are outlined in which this assumption is violated under changes of environmental conditions, such as the build execution platform or the JDK installed on the build host. This repository concentrates solely on Java builds. The artifact in question will always be the `main_deploy.jar` target which is [implicitly created by the java_binary](https://docs.bazel.build/versions/0.25.0/be/java.html#java_binary_implicit_outputs) rule.

This repository serves as a supplement to corresponding [github.com/bazelbuild/bazel](github.com/bazelbuild/bazel) bug reports.

## Case 3: Line endings (again!) in genrules

When a genrule from an external git repository is invoked on Windows, some system-wide `config.autocrlf` git configuration option is used. This may lead to extra `\r` characters in input files which are not present in the repository itself. These may be reproduced in the generated files, leading to differences in the generated files.

## Steps to reproduce

On a Windows system, run:
```
$ bazel build main_deploy.jar
```

Observe the file `bazel-out/x64_windows-fastbuild/bin/external/com_github_Xjs_bazel_java_aux/BazelTest.java` containing a `\r` character in line 5 (**not** at the end of the line; line endings emphasized for clarity):

```
package bazeltest;\n
\n
public final class BazelTest {\n
\n
  public static final int GENERATED = 42\r;\n
\n
  public static boolean compare(int something) {\n
    return GENERATED == something;\n
  }\n
}\n
```

On a UNIX system, run the same commands:
```
$ bazel build main_deploy.jar
```
Observe the file `bazel-out/k8-fastbuild/bin/external/com_github_Xjs_bazel_java_aux/BazelTest.java` **not** containing any `\r` character (line endings emphasized for clarity):

```
package bazeltest;\n
\n
public final class BazelTest {\n
\n
  public static final int GENERATED = 42;\n
\n
  public static boolean compare(int something) {\n
    return GENERATED == something;\n
  }\n
}\n
```

This difference does manifest in the resulting `.class` files which are contained in the `.jar` file.

## Leads

The git repository (declared in [WORKSPACE:5-9](WORKSPACE#L5-L9)) is pulled with the [`git_repository`](https://github.com/bazelbuild/bazel/blob/master/tools/build_defs/repo/git.bzl) rule. The default setting for git on Windows is to convert LF-only (UNIX-style) text files in repositories to CRLF (Windows-style) files in local copies. This setting is governed by git's `core.autocrlf` option. 

When git is invoked by the `git_repository` rule, `core.autocrlf` is likely true and hence the input file for our genrule, [`STATIC_INPUT`](https://github.com/Xjs/bazel-java-aux/blob/master/STATIC_INPUT), is converted to Windows-style line endings. Consequently, the line ending in this file is read by the genrule and inserted into the generated java file. This results in non-binary-identical build artifacts.

It would probably be reasonable to control the `autocrlf` option of the bazel-invoked git.
