package com.stli.weather_forecast.data.mapper

import com.stli.weather_forecast.data.remote.dto.GeocodingResponseDto
import com.stli.weather_forecast.data.remote.dto.GeocodingResultDto
import com.stli.weather_forecast.domain.model.City

fun GeocodingResultDto.toCity(): City = City(
    id = id,
    name = name,
    region = admin1,
    country = country,
    latitude = latitude,
    longitude = longitude,
)

fun GeocodingResponseDto.toCityList(): List<City> = results.map { it.toCity() }
