/*
 * Copyright 2024, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import io.spine.internal.dependency.JUnit
import io.spine.internal.gradle.isSnapshot
import io.spine.internal.gradle.publish.ChordsPublishing
import io.spine.internal.gradle.publish.SpinePublishing

plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    testImplementation(JUnit.runner)
}

val versionToPublish = project.version.toString()

val functionalTest: SourceSet by sourceSets.creating
gradlePlugin.testSourceSets(functionalTest)

configurations[functionalTest.implementationConfigurationName]
    .extendsFrom(configurations.testImplementation.get())

val functionalTestTask = tasks.register<Test>("functionalTest") {
    testClassesDirs = functionalTest.output.classesDirs
    classpath = configurations[functionalTest.runtimeClasspathConfigurationName]
        .plus(functionalTest.output)
}

tasks.withType(Test::class) {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

tasks.named("test") {
    dependsOn(functionalTestTask)
}

pluginBundle {
    website = "https://github.com/SpineEventEngine/Chords-Gradle-plugin/blob/master/README.md"
    vcsUrl = "https://github.com/SpineEventEngine/Chords-Gradle-plugin"
    tags = listOf("spine", "chords", "gradle", "plugin", "protobuf", "codegen", "compose")

    mavenCoordinates {
        groupId = "io.spine.chords"
        artifactId = "spine-chords-gradle-plugin"
        version = versionToPublish
    }
}

// Do not attempt to publish snapshot versions to comply with publishing rules.
// See: https://plugins.gradle.org/docs/publish-plugin#approval
val publishPlugins: Task by tasks.getting {
    enabled = !versionToPublish.isSnapshot()
}

val publish: Task by tasks.getting {
    dependsOn(publishPlugins)
}

gradlePlugin {
    plugins {
        create("chordsGradlePlugin") {
            id = ChordsPublishing.GradlePlugin.id
            displayName = "Chords Codegen Gradle Plugin"
            description = "A plugin that generates Kotlin extensions for Proto messages."
            implementationClass = "io.spine.chords.gradle.GradlePlugin"
        }
    }
}

publishing.publications.withType<MavenPublication>().all {
    groupId = "io.spine.chords"
    artifactId = "spine-chords-gradle-plugin"
}

// Add the common prefix to the `pluginMaven` publication.
//
// The publication is automatically created in `project.afterEvaluate` by Plugin Publishing plugin.
// See https://docs.gradle.org/current/userguide/java_gradle_plugin.html#maven_publish_plugin
//
// We add the prefix also in `afterEvaluate` assuming we're later in the sequence of
// the actions and the publications have been already created.
//
project.afterEvaluate {
    publishing {
        // Get the prefix used for all the modules of this project.
        val prefix = project.rootProject.the<SpinePublishing>().artifactPrefix

        // Add the prefix to the `pluginMaven` publication only.
        publications.named<MavenPublication>("pluginMaven") {
            if (!artifactId.startsWith(prefix)) {
                artifactId = prefix + artifactId
            }
        }

        // Do not add the prefix for the publication which produces
        // the `io.spine.chords.gradle.plugin` marker.

        // Add the prefix to the `artifactId` of the plugin dependency used in the marker.
        publications.withType<MavenPublication>().configureEach {
            if (name.endsWith("PluginMarkerMaven")) {
                pom.withXml {
                    asNode().apply {
                        val root = asElement()
                        // Get the `dependencies/dependency/artifactId` node.
                        // It's the second node with such a name from the top.
                        // The first one is the `artifactId` of the marker itself.
                        val artifactIdNode = root.getElementsByTagName("artifactId").item(1)
                        artifactIdNode.textContent = "$prefix${project.name}"
                    }
                }
            }
        }
    }
}

configureTaskDependencies()
