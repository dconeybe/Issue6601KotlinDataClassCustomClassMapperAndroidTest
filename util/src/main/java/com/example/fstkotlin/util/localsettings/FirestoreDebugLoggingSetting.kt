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

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

interface FirestoreDebugLoggingSetting {

  /**
   * Whether to enable Firestore debug logging.
   *
   * This value is typically loaded from the `firestore.debugLogging` property defined in
   * `fstkotlin.local.properties`, which is loaded by the
   * `com.example.fstkotlin.gradle.localsettings.LocalSettingsPlugin` Gradle plugin and propagated
   * to the code via Android string resources.
   */
  val firestoreDebugLogging: Boolean?
}

fun FirestoreDebugLoggingSetting.applyFirestoreDebugLogging(): AppliedFirestoreDebugLogging {
  val firestoreDebugLogging = this.firestoreDebugLogging
  if (firestoreDebugLogging === null) {
    return AppliedFirestoreDebugLogging(null, null)
  } else {
    Log.i(
      "FirestoreDebugLogging",
      "Setting FirebaseFirestore.setLoggingEnabled($firestoreDebugLogging)",
    )
    FirebaseFirestore.setLoggingEnabled(firestoreDebugLogging)
    // There is no way to determine the original value for the log level being enabled; therefore,
    // just assume that it was _not_ enabled and set restoreValue=false.
    return AppliedFirestoreDebugLogging(appliedValue = firestoreDebugLogging, restoreValue = false)
  }
}

data class AppliedFirestoreDebugLogging(val appliedValue: Boolean?, val restoreValue: Boolean?) {
  fun restore() {
    if (restoreValue !== null) {
      Log.i("FirestoreDebugLogging", "Restoring FirebaseFirestore.setLoggingEnabled($restoreValue)")
      FirebaseFirestore.setLoggingEnabled(restoreValue)
    }
  }
}
