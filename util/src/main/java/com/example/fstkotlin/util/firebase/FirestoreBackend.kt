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

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

/** The various Firestore backends against which integration tests can run. */
sealed interface FirestoreBackend {

  val firestoreSettings: FirebaseFirestoreSettings?
  val authBackend: FirebaseAuthBackend

  fun getFirestore(app: FirebaseApp): FirebaseFirestore {
    val firestore = FirebaseFirestore.getInstance(app)
    firestoreSettings?.let { firestore.firestoreSettings = it }
    return firestore
  }

  /** The "production" Firestore server, which is used by customers. */
  object Production : FirestoreBackend {
    override val firestoreSettings
      get() = null

    override val authBackend: FirebaseAuthBackend
      get() = FirebaseAuthBackend.Production

    override fun toString() = "FirestoreBackend.Production"
  }

  sealed class PredefinedFirestoreBackend(val host: String) : FirestoreBackend {
    override val firestoreSettings
      get() = FirebaseFirestoreSettings.Builder().setHost(host).build()

    override val authBackend: FirebaseAuthBackend
      get() = FirebaseAuthBackend.Production
  }

  /** The "nightly" Firestore server. */
  data object Nightly : PredefinedFirestoreBackend("test-firestore.sandbox.googleapis.com") {
    override fun toString() = "FirestoreBackend.Nightly($host)"
  }

  /** The "staging" Firestore server, also known as "QA". */
  data object Staging : PredefinedFirestoreBackend("staging-firestore.sandbox.googleapis.com") {
    override fun toString() = "FirestoreBackend.Staging($host)"
  }

  /** A custom Firestore server. */
  data class Custom(val host: String, val sslEnabled: Boolean) : FirestoreBackend {
    override val firestoreSettings
      get() = FirebaseFirestoreSettings.Builder().setHost(host).setSslEnabled(sslEnabled).build()

    override val authBackend: FirebaseAuthBackend
      get() = FirebaseAuthBackend.Production

    override fun toString() = "FirestoreBackend.Custom(host=$host, sslEnabled=$sslEnabled)"
  }

  /** The Firestore emulator. */
  data class Emulator(val host: String? = null, val port: Int? = null) : FirestoreBackend {
    override val firestoreSettings
      get() = null

    override val authBackend: FirebaseAuthBackend
      get() = FirebaseAuthBackend.Emulator()

    override fun toString() = "FirestoreBackend.Emulator(host=$host, port=$port)"

    override fun getFirestore(app: FirebaseApp): FirebaseFirestore {
      val firestore = super.getFirestore(app)
      firestore.useEmulator(host ?: DEFAULT_HOST, port ?: DEFAULT_PORT)
      return firestore
    }

    companion object {
      const val DEFAULT_HOST = "10.0.2.2"
      const val DEFAULT_PORT = 8080
    }
  }

  companion object {

    private fun URL.hostOrNull(): String? = host.ifEmpty { null }

    private fun URL.portOrNull(): Int? = port.let { if (it > 0) it else null }

    fun fromString(arg: String?): FirestoreBackend? {
      if (arg.isNullOrEmpty()) {
        return null
      }

      when (arg) {
        "prod" -> return Production
        "staging" -> return Staging
        "nightly" -> return Nightly
        "emulator" -> return Emulator()
      }

      val uri =
        try {
          URI(arg)
        } catch (e: URISyntaxException) {
          throw IllegalArgumentException(
            "value cannot be parsed as a URI: $arg ($e) (error code vhfwpfwjac)",
            e,
          )
        }

      if (uri.scheme == "emulator") {
        val url =
          try {
            URL("https://${uri.schemeSpecificPart}")
          } catch (e: MalformedURLException) {
            throw IllegalArgumentException(
              "invalid emulator URI: $arg ($e) (error code hfs4cwv7bs)",
              e,
            )
          }
        return Emulator(host = url.hostOrNull(), port = url.portOrNull())
      }

      val url =
        try {
          URL(arg)
        } catch (e: MalformedURLException) {
          throw IllegalArgumentException(
            "value cannot be parsed as a URL: $arg ($e) (error code tcc54n4h9n)",
            e,
          )
        }

      val host = url.hostOrNull()
      val port = url.portOrNull()
      val sslEnabled =
        when (url.protocol) {
          "http" -> false
          "https" -> true
          else ->
            throw IllegalArgumentException(
              "value $arg has an unsupported protocol: ${url.protocol} " +
                "(supported protocols are \"http\" and \"https\") (error code byvy24fapz)"
            )
        }

      val customHost =
        if (host !== null && port !== null) {
          "$host:$port"
        } else if (host !== null) {
          host
        } else if (port !== null) {
          ":$port"
        } else {
          throw IllegalArgumentException(
            "value must specify host and/or port: $arg (error code jzh9kbyhaf)"
          )
        }

      return Custom(host = customHost, sslEnabled = sslEnabled)
    }
  }
}
