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

import io.kotest.property.RandomSource

interface RandomSeedSetting {

  /**
   * The random seed to use.
   *
   * If `null`, then [RandomSource.default] should be used to obtain an instance; otherwise, specify
   * the value to [RandomSource.seeded] to obtain an instance.
   *
   * This value is typically loaded from the `randomSeed` property defined in
   * `fstkotlin.local.properties`, which is loaded by the
   * `com.example.fstkotlin.gradle.localsettings.LocalSettingsPlugin` Gradle plugin and propagated
   * to the code via Android string resources.
   */
  val randomSeed: Long?
}

/**
 * Creates and returns a new [RandomSource] object that reflects the value of
 * [RandomSeedSetting.randomSeed] of the receiver.
 */
fun RandomSeedSetting.newRandomSource(): RandomSourceWithSeed {
  val seed = randomSeed
  val rs =
    if (seed === null) {
      RandomSource.default()
    } else {
      RandomSource.seeded(seed)
    }
  return RandomSourceWithSeed(rs, seed)
}

data class RandomSourceWithSeed(val rs: RandomSource, val seed: Long?)
