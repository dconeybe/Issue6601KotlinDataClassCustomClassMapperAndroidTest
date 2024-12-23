/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fstkotlin.util.localsettings

import android.content.Context
import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fstkotlin.util.R
import java.util.WeakHashMap

/**
 * The local settings set in `fstkotlin.local.properties` by the Gradle plugin
 * `com.example.fstkotlin.gradle.localsettings.LocalSettingsPlugin`.
 */
class LocalSettings(
  private val context: Context = InstrumentationRegistry.getInstrumentation().context
) : FirestoreBackendSetting, FirestoreDebugLoggingSetting, RandomSeedSetting {

  override val firestoreBackend: String? by
    lazy(LazyThreadSafetyMode.PUBLICATION) { loadString(R.string.firestoreBackend) }

  override val firestoreDebugLogging: Boolean? by
    lazy(LazyThreadSafetyMode.PUBLICATION) { loadBoolean(R.string.firestoreDebugLogging) }

  override val randomSeed: Long? by
    lazy(LazyThreadSafetyMode.PUBLICATION) { loadLong(R.string.randomSeed) }

  override fun toString() =
    "LocalSettings(" +
      "firestoreBackend=$firestoreBackend, " +
      "firestoreDebugLogging=$firestoreDebugLogging, " +
      "randomSeed=$randomSeed" +
      ")"

  private fun loadString(@StringRes id: Int): String? =
    context.getString(id).trim().takeIf { it.isNotEmpty() }

  private fun loadBoolean(@StringRes id: Int): Boolean? {
    val stringValue = loadString(id) ?: return null
    return when (stringValue.lowercase()) {
      "true" -> true
      "false" -> false
      else ->
        throw IllegalArgumentException(
          "Unable to parse boolean value: $stringValue " +
            "(must be either \"true\", \"false\", or empty, case-insensitive) " +
            "(error code 8psktp7z3q)"
        )
    }
  }

  private fun loadLong(@StringRes id: Int): Long? {
    val stringValue = loadString(id) ?: return null
    return stringValue.toLongOrNull()
      ?: throw IllegalArgumentException(
        "Unable to parse Long value: $stringValue (error code czxrnfyzja)"
      )
  }

  companion object {

    private val instances = WeakHashMap<Context, LocalSettings>()

    fun fromInstrumentation(): LocalSettings {
      val context = InstrumentationRegistry.getInstrumentation().context
      synchronized(instances) {
        return instances.getOrPut(context) { LocalSettings(context) }
      }
    }
  }
}
