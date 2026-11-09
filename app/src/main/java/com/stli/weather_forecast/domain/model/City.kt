package com.stli.weather_forecast.domain.model

data class City(
    val id: Long,
    val name: String,
    val region: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
)
