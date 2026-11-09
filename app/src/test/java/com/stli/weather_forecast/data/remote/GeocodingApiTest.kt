package com.stli.weather_forecast.data.remote

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class GeocodingApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: GeocodingApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        // Die API liefert viele Felder, die die DTOs bewusst nicht deklarieren.
        val json = Json { ignoreUnknownKeys = true }
        api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(GeocodingApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `search parses the Berlin fixture into 10 results`() = runTest {
        server.enqueue(MockResponse().setBody(fixture("geocoding_berlin.json")))

        val response = api.search(name = "Berlin", count = 10, language = "en")

        assertEquals(10, response.results.size)
        val first = response.results.first()
        assertEquals(2950159L, first.id)
        assertEquals("Berlin", first.name)
        assertEquals(52.52437, first.latitude, 0.0)
        assertEquals(13.41053, first.longitude, 0.0)
        assertEquals("Germany", first.country)
        assertEquals("State of Berlin", first.admin1)
    }

    @Test
    fun `search returns an empty list when the response has no results field`() = runTest {
        server.enqueue(MockResponse().setBody(fixture("geocoding_no_results.json")))

        val response = api.search(name = "xqzxqz", count = 10, language = "en")

        assertEquals(emptyList<Any>(), response.results)
    }

    @Test
    fun `search sends name, count and language as query parameters to v1 search`() = runTest {
        server.enqueue(MockResponse().setBody(fixture("geocoding_no_results.json")))

        api.search(name = "Berlin", count = 5, language = "de")

        val url = server.takeRequest().requestUrl!!
        assertEquals("/v1/search", url.encodedPath)
        assertEquals("Berlin", url.queryParameter("name"))
        assertEquals("5", url.queryParameter("count"))
        assertEquals("de", url.queryParameter("language"))
    }

    @Test
    fun `search uses the default count when none is given`() = runTest {
        server.enqueue(MockResponse().setBody(fixture("geocoding_no_results.json")))

        api.search(name = "Berlin", language = "en")

        assertEquals("10", server.takeRequest().requestUrl!!.queryParameter("count"))
    }

    @Test
    fun `optional fields are null when a hit does not contain them`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"results":[{"id":1,"name":"Nowhere","latitude":1.0,"longitude":2.0}]}""",
            ),
        )

        val hit = api.search(name = "Nowhere", language = "en").results.single()

        assertNull(hit.country)
        assertNull(hit.admin1)
    }

    private fun fixture(name: String): String {
        val url = javaClass.classLoader?.getResource("fixtures/$name")
        return checkNotNull(url) { "Fixture $name fehlt" }.readText()
    }
}
