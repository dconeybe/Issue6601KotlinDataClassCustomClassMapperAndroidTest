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

package com.example.fstkotlin.util.junit

import android.util.Log
import com.example.fstkotlin.util.kotest.property.arbitrary.visuallyDistinctAlphanumericString
import com.example.fstkotlin.util.localsettings.LocalSettings
import com.example.fstkotlin.util.localsettings.RandomSeedSetting
import com.example.fstkotlin.util.localsettings.newRandomSource
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.next
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A JUnit test rule that prints the seed of a [RandomSource] if the test fails, enabling replaying
 * the test with the same seed to investigate failures. If the [Lazy] is never initialized, then the
 * random seed is _not_ printed.
 */
class RandomSourceRule(
  private val randomSeedSetting: RandomSeedSetting = LocalSettings.fromInstrumentation()
) : TestRule {

  private val id: String = nextId()

  lateinit var rs: RandomSource
    private set

  override fun apply(base: Statement, description: Description) =
    object : Statement() {

      override fun evaluate() {
        val (rs, seed) = randomSeedSetting.newRandomSource()
        val logMessage =
          if (seed !== null) {
            "Created RandomSource with configured seed: $seed"
          } else {
            "Created RandomSource with randomly-generated seed: ${rs.seed}"
          }
        Log.i("RandomSourceRule", "[$id] $logMessage")
        this@RandomSourceRule.rs = rs

        base
          .runCatching { evaluate() }
          .onFailure {
            Log.i(
              "RandomSourceRule",
              "[$id] Test ${description.displayName} failed " +
                "using RandomSource with seed=${rs.seed} " +
                "(error code gdp3vxstxx)",
            )
            throw it
          }
      }
    }

  companion object {
    private fun nextId(): String = "rsr" + Arb.visuallyDistinctAlphanumericString(size = 10).next()
  }
}
