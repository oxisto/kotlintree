# kotlintree

This little project provides Kotlin bindings for the popular [tree-sitter](http://github.com/tree-sitter/tree-sitter) library. Currently it only supports the Kotlin JVM target, but Kotlin native is on the roadmap (see [#3](https://github.com/oxisto/kotlintree/issues/3)).

It currently ships `tree-sitter` itself, as well as `tree-sitter-cpp`. We might want to include more languages (see [#2](https://github.com/oxisto/kotlintree/issues/2))

## Build

Just run `./gradlew build`, this should build everything you need into a packaged jar, including the necessary native libraries.

## Usage

For now, only local builds are supported. You can install the package using `./gradlew publishToMavenLocal`.

In your gradle.build.kts:
```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("io.github.oxisto:kotlin-tree-jna:0.0.0-SNAPSHOT")
}
```