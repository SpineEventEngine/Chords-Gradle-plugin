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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import java.io.File
import java.util.zip.ZipFile

/**
 * A Gradle [Plugin] that generates Kotlin extensions for Proto messages.
 *
 * Actually, it applies
 * [codegen/plugins](https://github.com/SpineEventEngine/Chords/tree/master/codegen/plugins)
 * to a module, which requires the code generation for Proto sources.
 *
 * It is required to configure the artifact of codegen plugins,
 * which should be used by the plugin for code generation.
 *
 * Adds a dependency for the `compileKotlin` task if it is present in the project.
 * Otherwise, it should be configured manually in the project build script.
 * The task `applyCodegenPlugins` should be configured to execute in this case.
 *
 * The dependencies on Proto sources that are required for code generation
 * can be configured in the build script.
 *
 * Below is an example configuration of the plugin:
 * ```
 * chordsGradlePlugin {
 *     codegenPluginsArtifact =
 *         "io.spine.chords:spine-chords-codegen-plugins:2.0.0-SNAPSHOT.27"
 *
 *     protoDependencies("io.spine:spine-money:1.5.0")
 * }
 * ```
 */
public class GradlePlugin : Plugin<Project> {

    @Suppress("ConstPropertyName")
    private companion object {
        private const val workspaceModuleName = "codegen-workspace"
    }

    /**
     * Creates plugin extension, which allows to configure the plugin,
     * and tasks that perform the necessary actions.
     */
    override fun apply(project: Project) {

        val workspaceDir = File(project.buildDir, workspaceModuleName)

        project.createExtension()

        val createCodegenWorkspace = project.tasks
            .register("createCodegenWorkspace") { task ->
                task.doLast {
                    createCodegenWorkspace(project, workspaceDir)
                }
            }

        val addGradleWrapperRunPermission = project.tasks
            .register("addGradleWrapperRunPermission") { task ->
                task.dependsOn(createCodegenWorkspace)
                task.doLast {
                    addGradleWrapperRunPermission(workspaceDir)
                }
            }

        val applyCodegenPlugins = project.tasks
            .register("applyCodegenPlugins", ApplyCodegenPlugins::class.java) { task ->
                task.dependsOn(addGradleWrapperRunPermission)
                task.workspaceDir = workspaceDir.path
                task.dependencies(project.extension.dependencies)
                task.codegenPluginsArtifact = project.extension.codegenPluginsArtifact
            }

        val compileKotlin = project.tasks.findByName("compileKotlin")
        if (compileKotlin != null) {
            compileKotlin.dependsOn(applyCodegenPlugins)
        } else {
            project.logger.warn(
                """
                Warning! `Chords-Gradle-plugin` will not be applied to module `${project.name}`.
                Task `compileKotlin` not found, so required dependency was not added.
                To generate code, execute the `applyCodegenPlugins` task before `compileKotlin`.
                """.trimIndent()
            )
        }
    }

    /**
     * Executes a native command to add run permission to `gradlew`.
     *
     * The operation is performed under Linux-based OS
     * and is skipped under Windows.
     */
    private fun addGradleWrapperRunPermission(workspaceDir: File) {
        if (OperatingSystem.current().isWindows) {
            return
        }
        ProcessBuilder()
            .command("chmod", "+x", "./gradlew")
            .directory(workspaceDir)
            .start()
    }

    /**
     * Copies the necessary resources from the `codegen-plugins` artifact.
     *
     * Actually, it creates a `workspace` module, in which the code generation
     * is to be performed.
     */
    private fun createCodegenWorkspace(project: Project, workspaceDir: File) {
        workspaceDir.mkdirs()
        val configurationName = "workspace"
        val workspace = project.configurations.create(configurationName) {
            it.isTransitive = false
        }
        project.dependencies.add(
            configurationName,
            project.extension.codegenPluginsArtifact
        )
        File(workspace.asPath).unzipTo(workspaceDir.parentFile) {
            it.contains(workspaceModuleName)
        }
    }
}

/**
 * Unpacks zip file to [destinationDir], applying the given [entryFilter] to the entries.
 */
public fun File.unzipTo(
    destinationDir: File,
    entryFilter: (entryName: String) -> Boolean
) {
    ZipFile(this).use { zipFile ->
        zipFile.entries().asSequence()
            .filter { entry ->
                !entry.isDirectory && entryFilter(entry.name)
            }
            .forEach { entry ->
                File(destinationDir, entry.name).also { destinationFile ->
                    destinationFile.parentFile.mkdirs()
                    zipFile.getInputStream(entry).copyTo(
                        destinationFile.outputStream()
                    )
                }
            }
    }
}

/**
 * Obtains the extension the plugin added to this Gradle project.
 */
private val Project.extension: ParametersExtension
    get() = extensions.getByType(ParametersExtension::class.java)
