/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fstkotlin.gradle.localsettings

import com.android.build.api.dsl.VariantDimension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.example.fstkotlin.gradle.util.TaggedLogger
import com.example.fstkotlin.gradle.util.nextAlphanumericString
import com.example.fstkotlin.gradle.util.withTag
import java.io.File
import java.io.FileNotFoundException
import java.util.Properties
import kotlin.random.Random
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.kotlin.dsl.create

@Suppress("unused")
abstract class LocalSettingsPlugin : Plugin<Project> {

  private val logTag = "LocalSettingsPlugin_" + Random.nextAlphanumericString(length = 10)

  override fun apply(project: Project) {
    val logger = project.logger.withTag(logTag)

    val ext = project.extensions.create<LocalSettingsPluginDslExtension>("fstkotlinLocalSettings")
    val applier =
      LocalSettingsPluginApplier(
        projectPath = project.path,
        ext = ext,
        localSettingsSearchDirectories = project.ancestors(),
        logger = logger,
      )

    project.plugins.withType(AppPlugin::class.java) { applier.apply(project) }
    project.plugins.withType(LibraryPlugin::class.java) { applier.apply(project) }
  }
}

interface LocalSettingsPluginDslExtension {

  /**
   * The file name of the "local settings" file to load.
   *
   * If `null`, use [DEFAULT_LOCAL_SETTINGS_FILE_NAME].
   */
  var localSettingsFileName: String?
}

const val DEFAULT_LOCAL_SETTINGS_FILE_NAME = "fstkotlin.local.properties"

private class LocalSettingsPluginApplier(
  private val projectPath: String,
  private val ext: LocalSettingsPluginDslExtension,
  private val localSettingsSearchDirectories: List<Directory>,
  private val logger: TaggedLogger,
) {
  fun apply(androidComponents: AndroidComponentsExtension<*, *, *>) {
    logger.info {
      "Applying plugin ${LocalSettingsPlugin::class.qualifiedName} to project $projectPath"
    }

    androidComponents.finalizeDsl { android ->
      val localSettingsFileName = ext.localSettingsFileName ?: DEFAULT_LOCAL_SETTINGS_FILE_NAME
      val localSettings =
        localSettingsSearchDirectories
          .map { it.file(localSettingsFileName) }
          .mapNotNull { LocalSettings.load(it, logger) }
          .reduce()

      android.defaultConfig.resourceSetter(logger).run {
        localSettings.run {
          stringRes("firestoreBackend", firestoreBackend)
          stringRes("firestoreDebugLogging", firestoreDebugLogging)
          stringRes("randomSeed", randomSeed)
        }
      }
    }
  }
}

private fun VariantDimension.resourceSetter(logger: TaggedLogger) = ResourceSetter(this, logger)

private class ResourceSetter(
  private val dimension: VariantDimension,
  private val logger: TaggedLogger,
) {

  fun stringRes(name: String, value: String?) {
    string(name, value?.trim() ?: "")
  }

  @JvmName("string0")
  private fun string(name: String, cleanValue: String) {
    logger.info { "Setting R.string.$name to: $cleanValue" }
    dimension.resValue("string", name, cleanValue)
  }
}

private fun LocalSettingsPluginApplier.apply(project: Project) {
  val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
  apply(androidComponents)
}

private data class LocalSettings(
  val firestoreBackend: String?,
  val firestoreDebugLogging: String?,
  val randomSeed: String?,
) {
  companion object
}

private fun LocalSettings.Companion.load(file: RegularFile, logger: TaggedLogger): LocalSettings? =
  load(file.asFile, logger)

private fun LocalSettings.Companion.load(file: File, logger: TaggedLogger): LocalSettings? {
  logger.info { "Loading local settings from file: ${file.absolutePath}" }
  val properties = Properties()
  try {
    file.inputStream().use { properties.load(it) }
  } catch (_: FileNotFoundException) {
    logger.info { "File not found: ${file.absolutePath}; skipping it" }
    return null
  }

  return LocalSettings(
      firestoreBackend = properties.getProperty("firestore.backend"),
      firestoreDebugLogging = properties.getProperty("firestore.debugLogging"),
      randomSeed = properties.getProperty("randomSeed"),
    )
    .also { logger.info { "Loaded ${file.absolutePath}: $it" } }
}

private fun Project.ancestors(): List<Directory> {
  val ancestors = mutableListOf<Directory>()
  var project: Project? = this
  while (project !== null) {
    ancestors.add(project.layout.projectDirectory)
    project = project.parent
  }
  return ancestors.toList()
}

private fun List<LocalSettings>.reduce(): LocalSettings {
  val reducedSettings = reduceOrNull { settings1, settings2 ->
    LocalSettings(
      firestoreBackend = settings1.firestoreBackend ?: settings2.firestoreBackend,
      firestoreDebugLogging = settings1.firestoreDebugLogging ?: settings2.firestoreDebugLogging,
      randomSeed = settings1.randomSeed ?: settings2.randomSeed,
    )
  }
  return reducedSettings ?: LocalSettings(null, null, null)
}
