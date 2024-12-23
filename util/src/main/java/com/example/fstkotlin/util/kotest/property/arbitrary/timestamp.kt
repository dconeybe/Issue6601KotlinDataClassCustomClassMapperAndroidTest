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

package com.example.fstkotlin.util.kotest.property.arbitrary

import com.google.firebase.Timestamp
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.Sample
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.asSample

@Suppress("UnusedReceiverParameter")
val FirebaseArbs.timestamp: FirebaseTimestampArbs
  get() = FirebaseTimestampArbs

fun FirebaseArbs.timestamp(): Arb<Timestamp> = timestamp.timestamp()

object FirebaseTimestampArbs {

  /** Returns an [Arb] that generates values that are valid for [Timestamp.nanoseconds]. */
  fun nanoseconds(): Arb<Int> = Arb.int(0..999_999_999)

  /** Returns an [Arb] that generates values that are valid for [Timestamp.seconds]. */
  fun seconds(): Arb<Long> = Arb.long(-62_135_596_800..253_402_300_799)

  /** Returns an [Arb] that generates [Timestamp] objects. */
  fun timestamp(
    seconds: Arb<Long> = seconds(),
    nanoseconds: Arb<Int> = nanoseconds(),
  ): Arb<Timestamp> = TimestampArbImpl(seconds = seconds, nanoseconds = nanoseconds)
}

private class TimestampArbImpl(private val seconds: Arb<Long>, private val nanoseconds: Arb<Int>) :
  Arb<Timestamp>() {

  private val edgeCaseArbs =
    Arb.choice(
      arbitrary { rs -> Pair(seconds.edgeCaseOrNext(rs), nanoseconds.next(rs)) },
      arbitrary { rs -> Pair(seconds.next(rs), nanoseconds.edgeCaseOrNext(rs)) },
      arbitrary { rs -> Pair(seconds.edgeCaseOrNext(rs), nanoseconds.edgeCaseOrNext(rs)) },
    )

  override fun edgecase(rs: RandomSource): Timestamp {
    val (seconds, nanoseconds) = edgeCaseArbs.next(rs)
    return Timestamp(seconds, nanoseconds)
  }

  override fun sample(rs: RandomSource): Sample<Timestamp> {
    val seconds = seconds.next(rs)
    val nanoseconds = nanoseconds.next(rs)
    return Timestamp(seconds, nanoseconds).asSample()
  }

  companion object {

    private fun <T> Arb<T>.edgeCaseOrNext(rs: RandomSource): T = edgecase(rs) ?: next(rs)
  }
}
