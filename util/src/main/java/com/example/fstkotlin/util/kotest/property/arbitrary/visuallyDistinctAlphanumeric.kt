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

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string

/**
 * The same as [Arb.Companion.string] but using [Codepoint.Companion.visuallyDistinctAlphanumeric].
 */
fun Arb.Companion.visuallyDistinctAlphanumericString(
  minSize: Int = 0,
  maxSize: Int = 100,
): Arb<String> =
  Arb.string(minSize = minSize, maxSize = maxSize, Codepoint.visuallyDistinctAlphanumeric())

/**
 * The same as [Arb.Companion.string] but using [Codepoint.Companion.visuallyDistinctAlphanumeric].
 */
fun Arb.Companion.visuallyDistinctAlphanumericString(size: Int): Arb<String> =
  Arb.string(size, Codepoint.visuallyDistinctAlphanumeric())

/**
 * The same as [Arb.Companion.string] but using [Codepoint.Companion.visuallyDistinctAlphanumeric].
 */
fun Arb.Companion.visuallyDistinctAlphanumericString(range: IntRange): Arb<String> =
  Arb.string(range, Codepoint.visuallyDistinctAlphanumeric())

// The set of characters comprising of the 10 numeric digits and the 26 lowercase letters of the
// English alphabet with some characters removed that can look similar in different fonts, like
// '1', 'l', and 'i'.
@Suppress("SpellCheckingInspection")
private const val ALPHANUMERIC_ALPHABET = "23456789abcdefghjkmnpqrstvwxyz"

/**
 * The same as [Codepoint.Companion.alphanumeric] but only returning _lowercase_ characters and
 * excluding characters that can be visually confused with other characters, such as the letter 'O'
 * and the number 0.
 */
fun Codepoint.Companion.visuallyDistinctAlphanumeric(): Arb<Codepoint> =
  Arb.of(ALPHANUMERIC_ALPHABET.toList().map { Codepoint(it.code) })
