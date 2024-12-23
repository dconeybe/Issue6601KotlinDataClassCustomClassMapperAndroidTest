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

import android.util.Log
import com.example.fstkotlin.util.localsettings.FirestoreBackendSetting
import com.example.fstkotlin.util.localsettings.LocalSettings
import com.example.fstkotlin.util.localsettings.newFirestoreBackend
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A JUnit test rule that creates instances of [FirebaseFirestore] for use during testing, and
 * closes them upon test completion.
 *
 * The [FirestoreBackend] used determined using the given [FirestoreBackendSetting] object.
 */
class FirestoreFactoryRule(
  private val firebaseAppFactory: FirebaseAppFactoryRule,
  private val firestoreBackendSetting: FirestoreBackendSetting = LocalSettings.fromInstrumentation(),
) : FactoryTestRule<FirebaseFirestore, Nothing>() {

  override fun createInstance(params: Nothing?): FirebaseFirestore {
    val (firestoreBackend, backendName) = firestoreBackendSetting.newFirestoreBackend()
    val firebaseApp = firebaseAppFactory.newInstance()
    val firestore = firestoreBackend.getFirestore(firebaseApp)
    Log.i(
      "FirestoreFactoryRule",
      "FirebaseFirestore created with FirebaseApp ${firebaseApp.name} " +
        "using Firestore backend $firestoreBackend " +
        "(from configured backend name: $backendName)",
    )
    return firestore
  }

  override fun destroyInstance(instance: FirebaseFirestore) {
    Log.i(
      "FirestoreFactoryRule",
      "Terminating FirebaseFirestore created with FirebaseApp ${instance.app.name} ",
    )
    Tasks.await(instance.terminate())
  }
}
