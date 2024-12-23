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

import com.example.fstkotlin.util.kotest.matchers.shouldContainWithNonAbuttingText
import com.example.fstkotlin.util.kotest.matchers.shouldContainWithNonAbuttingTextIgnoringCase
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.net.URI
import java.net.URL
import org.junit.Test

class FirestoreBackendUnitTest {

  @Test
  fun `fromString(null) should return null`() {
    FirestoreBackend.fromString(null).shouldBeNull()
  }

  @Test
  fun `fromString('prod') should return Production`() {
    FirestoreBackend.fromString("prod") shouldBeSameInstanceAs FirestoreBackend.Production
  }

  @Test
  fun `fromString('staging') should return Staging`() {
    FirestoreBackend.fromString("staging") shouldBeSameInstanceAs FirestoreBackend.Staging
  }

  @Test
  fun `fromString('nightly') should return Nightly`() {
    FirestoreBackend.fromString("nightly") shouldBeSameInstanceAs FirestoreBackend.Nightly
  }

  @Test
  fun `fromString('emulator') should return Emulator()`() {
    FirestoreBackend.fromString("emulator") shouldBe FirestoreBackend.Emulator()
  }

  @Test
  fun `fromString(emulator with host) should return Emulator() with the host`() {
    FirestoreBackend.fromString("emulator:a.b.c") shouldBe FirestoreBackend.Emulator(host = "a.b.c")
  }

  @Test
  fun `fromString(emulator with port) should return Emulator() with the port`() {
    FirestoreBackend.fromString("emulator::9987") shouldBe FirestoreBackend.Emulator(port = 9987)
  }

  @Test
  fun `fromString(emulator with host and port) should return Emulator() with the host and port`() {
    FirestoreBackend.fromString("emulator:a.b.c:9987") shouldBe
      FirestoreBackend.Emulator(host = "a.b.c", port = 9987)
  }

  @Test
  fun `fromString(http url with host) should return Custom()`() {
    FirestoreBackend.fromString("http://a.b.c") shouldBe FirestoreBackend.Custom("a.b.c", false)
  }

  @Test
  fun `fromString(http url with host and port) should return Custom()`() {
    FirestoreBackend.fromString("http://a.b.c:9987") shouldBe
      FirestoreBackend.Custom("a.b.c:9987", false)
  }

  @Test
  fun `fromString(https url with host) should return Custom()`() {
    FirestoreBackend.fromString("https://a.b.c") shouldBe FirestoreBackend.Custom("a.b.c", true)
  }

  @Test
  fun `fromString(https url with host and port) should return Custom()`() {
    FirestoreBackend.fromString("https://a.b.c:9987") shouldBe
      FirestoreBackend.Custom("a.b.c:9987", true)
  }

  @Test
  fun `fromString(invalid URI) should throw an exception`() {
    val exception = shouldThrow<IllegalArgumentException> { FirestoreBackend.fromString("..:") }

    val uriParseErrorMessage = runCatching { URI("..:") }.exceptionOrNull()!!.message!!
    assertSoftly {
      exception.message shouldContainWithNonAbuttingText "..:"
      exception.message shouldContainWithNonAbuttingTextIgnoringCase "cannot be parsed as a URI"
      exception.message shouldContainWithNonAbuttingText uriParseErrorMessage
      exception.message shouldContainWithNonAbuttingText "vhfwpfwjac"
    }
  }

  @Test
  fun `fromString(invalid emulator URI) should throw an exception`() {
    val exception =
      shouldThrow<IllegalArgumentException> { FirestoreBackend.fromString("emulator:::::") }

    val urlParseErrorMessage = runCatching { URL("https://::::") }.exceptionOrNull()!!.message!!
    assertSoftly {
      exception.message shouldContainWithNonAbuttingText "emulator:::::"
      exception.message shouldContainWithNonAbuttingTextIgnoringCase "invalid emulator URI"
      exception.message shouldContainWithNonAbuttingText urlParseErrorMessage
      exception.message shouldContainWithNonAbuttingText "hfs4cwv7bs"
    }
  }

  @Test
  fun `fromString(unsupported protocol) should throw an exception`() {
    val exception =
      shouldThrow<IllegalArgumentException> { FirestoreBackend.fromString("ftp://abc:123") }

    assertSoftly {
      exception.message shouldContainWithNonAbuttingText "ftp://abc:123"
      exception.message shouldContainWithNonAbuttingTextIgnoringCase "has an unsupported protocol"
      exception.message shouldContainWithNonAbuttingText "ftp"
      exception.message shouldContainWithNonAbuttingText "http"
      exception.message shouldContainWithNonAbuttingText "https"
      exception.message shouldContainWithNonAbuttingText "byvy24fapz"
    }
  }

  @Test
  fun `fromString(no host or port specified) should throw an exception`() {
    val exception =
      shouldThrow<IllegalArgumentException> { FirestoreBackend.fromString("http://?a=42") }

    assertSoftly {
      exception.message shouldContainWithNonAbuttingText "http://?a=42"
      exception.message shouldContainWithNonAbuttingTextIgnoringCase "must specify host and/or port"
      exception.message shouldContainWithNonAbuttingText "jzh9kbyhaf"
    }
  }
}
