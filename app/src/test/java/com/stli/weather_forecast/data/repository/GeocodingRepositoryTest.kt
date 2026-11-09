package com.stli.weather_forecast.data.repository

import com.stli.weather_forecast.data.remote.GeocodingApi
import com.stli.weather_forecast.di.NetworkModule
import com.stli.weather_forecast.domain.AppError
import com.stli.weather_forecast.domain.AppResult
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class GeocodingRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: GeocodingRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        // Dieselbe Json-Konfiguration wie im NetworkModule.
        val json = NetworkModule.provideJson()
        val api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(GeocodingApi::class.java)
        repository = GeocodingRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search returns cities for a match`() = runTest {
        server.enqueue(MockResponse().setBody(readFixture("geocoding_berlin.json")))
        val result = repository.search("Berlin")
        check(result is AppResult.Success) { "Expected Success but was $result" }
        val cities = result.data
        assertTrue(cities.isNotEmpty())
        assertEquals("Berlin", cities.first().name)
    }

    @Test
    fun `search returns empty list when there are no results`() = runTest {
        server.enqueue(MockResponse().setBody(readFixture("geocoding_no_results.json")))
        val result = repository.search("wzwzwzw")
        check(result is AppResult.Success) { "Expected Success but was $result" }
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `search returns server error on http 500`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val result = repository.search("")
        check(result is AppResult.Failure) { "Expected Failure but was $result" }
        assertEquals(AppError.Server(500), result.error)
    }

    @Test
    fun `search passes on the status code on http 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        val result = repository.search("Berlin")
        check(result is AppResult.Failure) { "Expected Failure but was $result" }
        assertEquals(AppError.Server(404), result.error)
    }

    @Test
    fun `search returns network error when the connection breaks`() = runTest {
        // OkHttp wiederholt einen abgebrochenen Verbindungsaufbau einmal, daher zwei Abbrueche.
        repeat(2) {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        }
        val result = repository.search("Berlin")
        check(result is AppResult.Failure) { "Expected Failure but was $result" }
        assertEquals(AppError.Network, result.error)
    }

    @Test
    fun `search returns unknown error for an invalid response body`() = runTest {
        server.enqueue(MockResponse().setBody("kein json"))
        val result = repository.search("Berlin")
        check(result is AppResult.Failure) { "Expected Failure but was $result" }
        assertEquals(AppError.Unknown, result.error)
    }

    @Test
    fun `search does not swallow cancellation`() = runTest {
        // Die Antwort kommt erst nach 5 Sekunden, der Request haengt also fest.
        server.enqueue(
            MockResponse()
                .setBody("{}")
                .setHeadersDelay(5, TimeUnit.SECONDS),
        )
        var result: AppResult<*>? = null

        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            result = repository.search("Berlin")
        }
        // Warten, bis der Request wirklich beim Server angekommen ist, dann abbrechen.
        assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        // Wuerde das Repository die Cancellation schlucken, stuende hier ein Failure(Unknown).
        assertNull(result)
    }

    private fun readFixture(name: String): String {
        val url = javaClass.classLoader?.getResource("fixtures/$name")
        return checkNotNull(url) { "Fixture $name fehlt" }.readText()
    }
}
