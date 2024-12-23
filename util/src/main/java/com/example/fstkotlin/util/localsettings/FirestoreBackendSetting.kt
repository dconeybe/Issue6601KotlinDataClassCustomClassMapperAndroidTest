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

import com.example.fstkotlin.util.firebase.FirestoreBackend

interface FirestoreBackendSetting {

  /**
   * The Firestore backend.
   *
   * This value can be specified to [FirestoreBackend.fromString] to get an appropriate instance.
   *
   * This value is typically loaded from the `firestore.backend` property defined in
   * `fstkotlin.local.properties`, which is loaded by the
   * `com.example.fstkotlin.gradle.localsettings.LocalSettingsPlugin` Gradle plugin and propagated
   * to the code via Android string resources.
   */
  val firestoreBackend: String?
}

/**
 * Returns a [FirestoreBackend] object that reflects the value of
 * [FirestoreBackendSetting.firestoreBackend] of the receiver.
 */
fun FirestoreBackendSetting.newFirestoreBackend(): FirestoreBackendStringPair {
  val firestoreBackendString = this.firestoreBackend
  val firestoreBackend =
    FirestoreBackend.fromString(firestoreBackendString) ?: FirestoreBackend.Emulator()
  return FirestoreBackendStringPair(firestoreBackend, firestoreBackendString)
}

data class FirestoreBackendStringPair(
  val firestoreBackend: FirestoreBackend,
  val backendString: String?,
)
