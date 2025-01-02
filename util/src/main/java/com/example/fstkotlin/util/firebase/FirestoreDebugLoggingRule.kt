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

package com.example.fstkotlin.util.firebase

import com.example.fstkotlin.util.localsettings.AppliedFirestoreDebugLogging
import com.example.fstkotlin.util.localsettings.FirestoreDebugLoggingSetting
import com.example.fstkotlin.util.localsettings.LocalSettings
import com.example.fstkotlin.util.localsettings.applyFirestoreDebugLogging
import org.junit.rules.ExternalResource

/**
 * A JUnit test rule that enables Firestore debug logging, as specified by the given
 * [FirestoreDebugLoggingSetting] object.
 */
class FirestoreDebugLoggingRule(
  private val firestoreDebugLoggingSetting: FirestoreDebugLoggingSetting =
    LocalSettings.fromInstrumentation()
) : ExternalResource() {

  constructor(
    debugLoggingEnabled: Boolean
  ) : this(
    object : FirestoreDebugLoggingSetting {
      override val firestoreDebugLogging = debugLoggingEnabled
    }
  )

  private lateinit var appliedDebugLogging: AppliedFirestoreDebugLogging

  override fun before() {
    appliedDebugLogging = firestoreDebugLoggingSetting.applyFirestoreDebugLogging()
  }

  override fun after() {
    appliedDebugLogging.restore()
  }
}
