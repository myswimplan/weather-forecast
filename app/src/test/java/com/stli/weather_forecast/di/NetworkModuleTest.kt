package com.stli.weather_forecast.di

import com.stli.weather_forecast.data.remote.dto.GeocodingResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NetworkModuleTest {
    @Test
    fun `json ignores unknown keys`() {
        val json = NetworkModule.provideJson()
        val body = """{"results":[{"id":1,"name":"A","latitude":1.0,"longitude":2.0,"timezone":"x"}],"other":1}"""

        val response = json.decodeFromString<GeocodingResponseDto>(body)

        assertEquals(1, response.results.size)
    }

    @Test
    fun `json reads the Berlin fixture with its many extra fields`() {
        val json = NetworkModule.provideJson()
        val body =
            checkNotNull(javaClass.classLoader?.getResource("fixtures/geocoding_berlin.json")) {
                "Fixture fehlt"
            }.readText()

        assertEquals(10, json.decodeFromString<GeocodingResponseDto>(body).results.size)
    }

    @Test
    fun `retrofit points to the geocoding host and uses the given client`() {
        val client = NetworkModule.provideOkHttpClient()
        val retrofit = NetworkModule.provideRetrofit(client, NetworkModule.provideJson())

        assertEquals("https://geocoding-api.open-meteo.com/", retrofit.baseUrl().toString())
        assertSame(client, retrofit.callFactory())
    }
}
