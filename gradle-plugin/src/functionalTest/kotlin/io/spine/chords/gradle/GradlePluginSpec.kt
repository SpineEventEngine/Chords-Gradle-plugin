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

package io.spine.chords.gradle

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.FileWriter
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * The functional test for `io.spine.chords.gradle` Gradle plugin.
 */
@DisplayName("Gradle plugin should")
class GradlePluginSpec {

    @Suppress("ConstPropertyName")
    private companion object {
        /**
         * The `id` of the plugin to apply.
         */
        private const val pluginId = "io.spine.chords"

        /**
         * The Spine cloud artifacts repo to load the `codegen-plugins` from.
         */
        private const val spineArtifactsRepo =
            "https://europe-maven.pkg.dev/spine-event-engine"

        /**
         * Source Proto file to generate the code for.
         */
        private const val sourceProtoFile =
            "src/main/proto/chords/commands.proto"

        /**
         * The Kotlin file that should be generated by the plugin.
         */
        private const val expectedKotlinFile =
            "generated/main/kotlin/io/chords/command/TestCommandDef.kt"
    }

    /**
     * Checks that the required tasks are executed by the `GradlePlugin`
     * and Kotlin code is generated for Proto files.
     */
    @Test
    @DisplayName("copy resources and apply codegen plugins")
    fun copyResourcesAndApplyCodegenPlugins() {
        val projectDir = File("build/functionalTest")
        projectDir.mkdirs()

        File(projectDir, "settings.gradle.kts").writeText("")
        File(projectDir, "build.gradle.kts").writeText(
            generateGradleBuildFile(pluginId, spineArtifactsRepo)
        )
        File(projectDir, sourceProtoFile).writeText(
            protoFileContent
        )

        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("applyCodegenPlugins")
            .withProjectDir(projectDir)
            .build()

        listOf(
            "> Task :createCodegenWorkspace",
            "> Task :addGradleWrapperRunPermission",
            "> Task :applyCodegenPlugins",
            "BUILD SUCCESSFUL"
        ).forEach { message ->
            result.output.contains(message) shouldBe true
        }

        withClue("The required Kotlin file has not been generated.") {
            File(projectDir, expectedKotlinFile).exists() shouldBe true
        }
    }
}

/**
 * Writes the specified [text] to this File.
 */
private fun File.writeText(text: String) {
    parentFile.mkdirs()
    FileWriter(this).use { writer ->
        writer.write(text)
    }
}
