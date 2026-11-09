package com.stli.weather_forecast.domain.repository

import com.stli.weather_forecast.domain.AppResult
import com.stli.weather_forecast.domain.model.City

interface GeocodingRepository {
    suspend fun search(query: String): AppResult<List<City>>
}
