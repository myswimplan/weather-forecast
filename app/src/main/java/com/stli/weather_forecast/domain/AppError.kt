package com.stli.weather_forecast.domain

sealed interface AppError {
    /** Kein Internet oder Verbindung fehlgeschlagen. */
    data object Network : AppError

    /** Der Server hat mit einem HTTP-Fehlerstatus geantwortet. */
    data class Server(val code: Int) : AppError

    /** Alles, was nicht als Network oder Server erkannt wurde. */
    data object Unknown : AppError
}
