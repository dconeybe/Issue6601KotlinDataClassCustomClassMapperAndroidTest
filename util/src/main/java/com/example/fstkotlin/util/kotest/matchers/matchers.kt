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

package com.example.fstkotlin.util.kotest.matchers

import io.kotest.assertions.print.print
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.neverNullMatcher
import io.kotest.matchers.should
import java.util.regex.Pattern

/**
 * Creates and returns a [Matcher] that can be used with kotest assertions for verifying that a
 * string contains the given string with non-abutting text. See [shouldContainWithNonAbuttingText]
 * for full details.
 */
fun containWithNonAbuttingText(s: String, ignoreCase: Boolean = false): Matcher<String?> =
  neverNullMatcher { value ->
    val fullPattern = "(^|\\W)${Pattern.quote(s)}($|\\W)"
    val expr =
      if (ignoreCase) {
        Pattern.compile(fullPattern)
      } else {
        Pattern.compile(fullPattern, Pattern.CASE_INSENSITIVE)
      }

    MatcherResult(
      expr.matcher(value).find(),
      {
        "${value.print().value} should contain the substring ${s.print().value} with non-abutting text"
      },
      {
        "${value.print().value} should not contain the substring ${s.print().value} with non-abutting text"
      },
    )
  }

/**
 * Asserts that a string contains another string, verifying that the character immediately preceding
 * the text, if any, is a non-word character, and that the character immediately following the text,
 * if any, is also a non-word character. This effectively verifies that the given string is included
 * in a string without being "mashed" into adjacent text, such as can happen when constructing error
 * messages and forgetting to leave a space between words.
 */
infix fun String?.shouldContainWithNonAbuttingText(s: String): String? {
  this should containWithNonAbuttingText(s, ignoreCase = false)
  return this
}

/** Same as [shouldContainWithNonAbuttingText] but ignoring case. */
infix fun String?.shouldContainWithNonAbuttingTextIgnoringCase(s: String): String? {
  this should containWithNonAbuttingText(s, ignoreCase = false)
  return this
}
