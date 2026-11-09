package com.stli.weather_forecast.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResponseDto(
    // Fehlt komplett, wenn die Suche nichts findet (siehe geocoding_no_results.json).
    val results: List<GeocodingResultDto> = emptyList(),
)
