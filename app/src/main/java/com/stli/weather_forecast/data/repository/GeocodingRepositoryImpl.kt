package com.stli.weather_forecast.data.repository

import com.stli.weather_forecast.data.mapper.toCityList
import com.stli.weather_forecast.data.remote.GeocodingApi
import com.stli.weather_forecast.domain.AppError
import com.stli.weather_forecast.domain.AppResult
import com.stli.weather_forecast.domain.model.City
import com.stli.weather_forecast.domain.repository.GeocodingRepository
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

class GeocodingRepositoryImpl
@Inject
constructor(
    private val api: GeocodingApi,
) : GeocodingRepository {
    override suspend fun search(query: String): AppResult<List<City>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) AppResult.Success(emptyList()) else fetchCities(trimmed)
    }

    private suspend fun fetchCities(query: String): AppResult<List<City>> = try {
        AppResult.Success(api.search(query, language = Locale.getDefault().language).toCityList())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        AppResult.Failure(AppError.Server(e.code()))
    } catch (ignored: IOException) {
        AppResult.Failure(AppError.Network)
    } catch (ignored: Exception) {
        AppResult.Failure(AppError.Unknown)
    }
}
