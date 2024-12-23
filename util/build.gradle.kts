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

plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
  id("com.example.fstkotlin.gradle.localsettings")
}

android {
  namespace = "com.example.fstkotlin.util"
  compileSdk = 35

  defaultConfig {
    minSdk = 21
    targetSdk = 35
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions { jvmTarget = "1.8" }
}

fstkotlinLocalSettings { localSettingsFileName = "fstkotlin.local.properties" }

dependencies {
  implementation(platform(libs.firebase.bom))
  compileOnly("com.google.firebase:firebase-auth")
  compileOnly("com.google.firebase:firebase-firestore")
  compileOnly(libs.testing.androidx.test.runner)
  compileOnly(libs.testing.junit)
  compileOnly(libs.testing.kotest.property)

  testImplementation(libs.testing.junit)
  testImplementation(libs.testing.kotest.assertions)
}
