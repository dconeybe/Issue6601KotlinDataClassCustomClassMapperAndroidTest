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

@file:OptIn(ExperimentalKotest::class)

package com.example.fstkotlin

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fstkotlin.util.firebase.FirebaseAppFactoryRule
import com.example.fstkotlin.util.firebase.FirestoreDebugLoggingRule
import com.example.fstkotlin.util.firebase.FirestoreFactoryRule
import com.example.fstkotlin.util.firebase.withMicrosecondPrecision
import com.example.fstkotlin.util.junit.RandomSourceRule
import com.example.fstkotlin.util.kotest.property.arbitrary.firebase
import com.example.fstkotlin.util.kotest.property.arbitrary.timestamp
import com.example.fstkotlin.util.kotest.property.arbitrary.visuallyDistinctAlphanumericString
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import io.kotest.assertions.withClue
import io.kotest.common.ExperimentalKotest
import io.kotest.matchers.longs.shouldBeBetween
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.next
import io.kotest.property.checkAll
import java.time.Duration
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

  @get:Rule val firestoreDebugLoggingRule = FirestoreDebugLoggingRule()
  @get:Rule val randomSourceRule = RandomSourceRule()
  @get:Rule val firebaseAppFactory = FirebaseAppFactoryRule(randomSourceRule)
  @get:Rule val firestoreFactory = FirestoreFactoryRule(firebaseAppFactory = firebaseAppFactory)

  val rs: RandomSource
    get() = randomSourceRule.rs

  val firestore: FirebaseFirestore by lazy { firestoreFactory.newInstance() }

  @Test
  fun noAnnotations() = runTest {
    data class Pojo(val myField: Timestamp)
    verifyRoundTrip("myField", TimestampFieldVerifier.EqualsWrittenValue) {
      Pair(Pojo(it), Pojo::myField)
    }
  }

  @Test
  fun propertyNameAppliedToProperty() = runTest {
    data class Pojo(@PropertyName("myRenamedField") val myField: Timestamp)
    verifyRoundTrip("myField", TimestampFieldVerifier.EqualsWrittenValue) {
      Pair(Pojo(it), Pojo::myField)
    }
  }

  @Test
  fun propertyNameAppliedToPropertyGetter() = runTest {
    data class Pojo(@get:PropertyName("myRenamedField") val myField: Timestamp)
    verifyRoundTrip("myRenamedField", TimestampFieldVerifier.EqualsWrittenValue) {
      Pair(Pojo(it), Pojo::myField)
    }
  }

  @Test
  fun serverTimestampAppliedToProperty() = runTest {
    data class Pojo(@ServerTimestamp val myField: Timestamp?)
    verifyRoundTrip("myField", TimestampFieldVerifier.ServerTimestampPopulated()) {
      Pair(Pojo(null), Pojo::myField)
    }
  }

  @Test
  fun serverTimestampAppliedToPropertyGetter() = runTest {
    data class Pojo(@get:ServerTimestamp val myField: Timestamp?)
    verifyRoundTrip("myField", TimestampFieldVerifier.ServerTimestampPopulated()) {
      Pair(Pojo(null), Pojo::myField)
    }
  }

  @Test
  fun serverTimestampAndPropertyNameAppliedToProperty() = runTest {
    data class Pojo(@PropertyName("foobar") @ServerTimestamp val myField: Timestamp)
    verifyRoundTrip("myField", TimestampFieldVerifier.EqualsWrittenValue) {
      Pair(Pojo(it), Pojo::myField)
    }
  }

  @Test
  fun serverTimestampAndPropertyNameAppliedToGetter() = runTest {
    data class Pojo(@get:PropertyName("foobar") @get:ServerTimestamp val myField: Timestamp?)
    verifyRoundTrip("foobar", TimestampFieldVerifier.ServerTimestampPopulated()) {
      Pair(Pojo(null), Pojo::myField)
    }
  }

  @Test
  fun serverTimestampAppliedToPropertyAndPropertyNameAppliedToGetter() = runTest {
    data class Pojo(@get:PropertyName("foobar") @ServerTimestamp val myField: Timestamp)
    verifyRoundTrip("foobar", TimestampFieldVerifier.EqualsWrittenValue) {
      Pair(Pojo(it), Pojo::myField)
    }
  }

  @Test
  fun serverTimestampAppliedToGetterAndPropertyNameAppliedToProperty() = runTest {
    data class Pojo(@PropertyName("foobar") @get:ServerTimestamp val myField: Timestamp?)
    verifyRoundTrip("myField", TimestampFieldVerifier.ServerTimestampPopulated()) {
      Pair(Pojo(null), Pojo::myField)
    }
  }
}

private sealed interface TimestampFieldVerifier {

  suspend fun before(test: ExampleInstrumentedTest) {}

  fun verify(writtenTimestamp: Timestamp?, retrievedTimestamp: Timestamp)

  data object EqualsWrittenValue : TimestampFieldVerifier {

    override fun verify(writtenTimestamp: Timestamp?, retrievedTimestamp: Timestamp) {
      writtenTimestamp.shouldNotBeNull()
      retrievedTimestamp shouldBe writtenTimestamp.withMicrosecondPrecision()
    }
  }

  class ServerTimestampPopulated : TimestampFieldVerifier {

    private data class BeforeData(val timeNs: Long, val serverTimestamp: Timestamp)

    private lateinit var beforeData: BeforeData

    override suspend fun before(test: ExampleInstrumentedTest) {
      val timeNs = System.nanoTime()
      val serverTimestamp = test.getServerTimestamp()
      beforeData = BeforeData(timeNs, serverTimestamp)
    }

    override fun verify(writtenTimestamp: Timestamp?, retrievedTimestamp: Timestamp) {
      val duration =
        Duration.between(beforeData.serverTimestamp.toInstant(), retrievedTimestamp.toInstant())
      val actualElapsedMillis = duration.toMillis()
      val currentTimeNs = System.nanoTime()
      val maxElapsedMillis = (currentTimeNs - beforeData.timeNs) / 1_000_000
      withClue(
        "currentTimeNs=$currentTimeNs actualElapsedMillis=$actualElapsedMillis " +
          "maxElapsedMillis=$maxElapsedMillis"
      ) {
        actualElapsedMillis.shouldBeBetween(0, maxElapsedMillis)
      }
    }

    override fun toString(): String =
      "ServerTimestampPopulated(beforeData=" +
        (if (::beforeData.isInitialized) "$beforeData" else "null") +
        ")"
  }
}

private suspend fun <T : Any> ExampleInstrumentedTest.verifyRoundTrip(
  fieldName: String,
  timestampFieldVerifier: TimestampFieldVerifier,
  pojoFactory: (Timestamp) -> Pair<T, T.() -> Timestamp?>,
) {
  timestampFieldVerifier.before(this)
  val documentReference = randomDocument()
  val propTestConfig = PropTestConfig(seed = rs.random.nextLong(), iterations = NUM_ITERATIONS)
  checkAll(propTestConfig, Arb.firebase.timestamp()) { timestamp ->
    val (pojo, writtenTimestampRetriever) = pojoFactory(timestamp)
    val writtenTimestamp = writtenTimestampRetriever(pojo)
    documentReference.set(pojo).await()
    val retrievedData = documentReference.get().await()
    withClue("retrieved data keys") { retrievedData.data?.keys?.toSet() shouldBe setOf(fieldName) }
    val retrievedTimestamp =
      withClue("retrieved timestamp field") {
        retrievedData.data?.get(fieldName).shouldBeInstanceOf<Timestamp>()
      }
    withClue(
      "verifying using: $timestampFieldVerifier " +
        "(writtenTimestamp=$writtenTimestamp retrievedTimestamp=$retrievedTimestamp)"
    ) {
      timestampFieldVerifier.verify(
        writtenTimestamp = writtenTimestamp,
        retrievedTimestamp = retrievedTimestamp,
      )
    }
  }
}

private suspend fun ExampleInstrumentedTest.getServerTimestamp(): Timestamp {
  val documentReference = randomDocument()
  documentReference.set(mapOf("3kqqgwjh4d" to FieldValue.serverTimestamp())).await()
  val snapshot = documentReference.get().await()
  return snapshot.getTimestamp("3kqqgwjh4d")!!
}

private fun ExampleInstrumentedTest.randomCollection(): CollectionReference {
  val name = "coll_" + randomString()
  return firestore.collection(name)
}

private fun ExampleInstrumentedTest.randomDocument(
  collectionReference: CollectionReference = randomCollection()
): DocumentReference {
  val name = "doc_" + randomString()
  return collectionReference.document(name)
}

private fun ExampleInstrumentedTest.randomString(): String =
  Arb.visuallyDistinctAlphanumericString(size = 40).next(rs)

private const val NUM_ITERATIONS = 20
