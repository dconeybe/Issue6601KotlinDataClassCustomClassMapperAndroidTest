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

import android.os.Handler
import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import com.example.fstkotlin.util.junit.RandomSourceRule
import com.example.fstkotlin.util.kotest.property.arbitrary.visuallyDistinctAlphanumericString
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.app
import com.google.firebase.initialize
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.next

/**
 * A JUnit test rule that creates instances of [FirebaseApp] for use during testing, and closes them
 * upon test completion.
 */
class FirebaseAppFactoryRule(private val randomSourceRule: RandomSourceRule) :
  FactoryTestRule<FirebaseApp, Nothing>() {

  private val rs: RandomSource
    get() = randomSourceRule.rs

  override fun createInstance(params: Nothing?): FirebaseApp {
    val defaultFirebaseApp = Firebase.app
    val context = defaultFirebaseApp.applicationContext
    val options = defaultFirebaseApp.options
    val appName = "test-app-" + Arb.visuallyDistinctAlphanumericString(size = 20).next(rs)
    val firebaseApp = Firebase.initialize(context, options, appName)
    Log.i("FirebaseAppFactoryRule", "FirebaseApp created with name: $appName")
    return firebaseApp
  }

  override fun destroyInstance(instance: FirebaseApp) {
    // Work around app crash due to IllegalStateException from FirebaseAuth if `delete()` is called
    // very quickly after `FirebaseApp.getInstance()`. See b/378116261 for details.
    deleteHandler.postDelayed(instance::delete, 1000)
  }

  companion object {
    private val deleteHandler: Handler by lazy {
      val handlerThread =
        HandlerThread("FirebaseAppFactoryRule.deleteHandler", THREAD_PRIORITY_BACKGROUND)
      handlerThread.start()
      Handler(handlerThread.looper)
    }
  }
}
