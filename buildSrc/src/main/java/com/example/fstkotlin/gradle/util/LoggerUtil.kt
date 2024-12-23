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
package com.example.fstkotlin.gradle.util

import org.gradle.api.logging.Logger

fun Logger.withTag(tag: String): TaggedLogger = TaggedLogger(this, tag)

class TaggedLogger(val logger: Logger, val tag: String) {

  inline fun info(block: () -> String) {
    if (logger.isInfoEnabled) {
      logger.info("[{}] {}", tag, block())
    }
  }
}
