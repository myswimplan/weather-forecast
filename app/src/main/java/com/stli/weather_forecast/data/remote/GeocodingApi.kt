package com.stli.weather_forecast.data.remote

import com.stli.weather_forecast.data.remote.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

private const val DEFAULT_COUNT = 10

interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = DEFAULT_COUNT,
        @Query("language") language: String,
    ): GeocodingResponseDto
}
