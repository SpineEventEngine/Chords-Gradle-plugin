[![Ubuntu build][ubuntu-build-badge]][gh-actions]
[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
# Spine Chords Gradle plugin

Gradle plugin for [Spine Chords](https://github.com/SpineEventEngine/Chords/),
a suite of libraries for developing desktop UI using the Compose Multiplatform toolkit.
Being based on the [Spine Event Engine](https://spine.io/), the Chords libraries use 
the [Protobuf](https://protobuf.dev/) to define the project's model. The Gradle plugin 
is designed to generate code that helps to utilize the Protobuf message API, generated by Protobuf 
compiler, and exposes its own API, allowing for more flexible and generic way of handling
Protobuf messages.

So, the following code will be generated if the plugin is applied to a project:

- [MessageDef](https://github.com/SpineEventEngine/Chords/blob/master/codegen/runtime/src/main/kotlin/io/spine/chords/runtime/MessageDef.kt) 
implementations for Proto messages. 
- [MessageField](https://github.com/SpineEventEngine/Chords/blob/master/codegen/runtime/src/main/kotlin/io/spine/chords/runtime/MessageField.kt) 
implementations for the fields of Proto messages.
- [MessageOneof](https://github.com/SpineEventEngine/Chords/blob/master/codegen/runtime/src/main/kotlin/io/spine/chords/runtime/MessageOneof.kt) 
implementations for the `oneof` fields of Proto messages.
- Some other useful Kotlin extensions, e.g. `ValidatingBuilder.messageDef()` 
that returns the instance of `MessageDef` implementation for the current message builder.

## Requirements
- Java 11
- Gradle `6.9.x`
- Kotlin `1.8.20`

The supported Gradle version is chosen because the Chords libraries are currently 
based on Spine `1.9.x`, which requires that Gradle version. 
Support for newer Gradle versions is expected to increase as new versions of Spine are released.

## How it works

Actually, the plugin applies [codegen-plugins](https://github.com/SpineEventEngine/Chords/blob/master/codegen/plugins/README.md)
to a project, which requires the code generation.

The `codegen-plugins`, which generate the code, require the newer version of Gradle,
`7.6.x` at the moment, comparing to Chords-based projects, which require Gradle version `6.9.x`.

So, in order to apply `codegen-plugins`, this Gradle plugin creates so-called
`codegen-workspace` project, which is configured with Gradle `7.6.x` and serves
as a place where code generation should be performed. The plugin then copies
Proto sources from the original project to the `codegen-workspace` and runs
code generation. After generating the Kotlin extensions, it will be copied back
to the original project.


## How to use

The following configuration should be added to the `build.gradle.kts` to apply the plugin:
```kotlin
plugins {
    id("io.spine.chords") version "1.9.10" // Specify the actual version here.
}

// Plugin configuration.
//
chordsGradlePlugin {  
    
    // Specify the artifact of `codegen-plugins` that will be used for code generation.
    //
    // This property is required.
    //
    // The version or even name will differ in your case.
    //
    codegenPluginsArtifact = 
        "io.spine.chords:spine-chords-codegen-plugins:2.0.0-SNAPSHOT.35"
    
    // Specify the Proto dependencies that are required for code generation.
    //
    // This section is optional.
    //
    // Enumerate the libraries, onto which the processed Proto sources depend.
    //
    protoDependencies(
        "io.spine:spine-money:1.5.0"
    ) 
}
```

The plugin adds a dependency for the `compileKotlin` task if it is present in the project.
Otherwise, it should be configured manually in the project's build script 
so that the `applyCodegenPlugins` task is run.

Below is an example configuration, which may differ in your case:
```kotlin
tasks.named("compileKotlin") {
    dependsOn(
        tasks.named("applyCodegenPlugins")
    )
}
```

[gh-actions]: https://github.com/SpineEventEngine/Chords-Gradle-plugin/actions
[ubuntu-build-badge]: https://github.com/SpineEventEngine/Chords-Gradle-plugin/actions/workflows/build-on-ubuntu.yml/badge.svg
