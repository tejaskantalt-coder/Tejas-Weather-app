package com.example.data.api

import com.example.data.model.OpenWeatherCurrentResponse
import com.example.data.model.OpenWeatherForecastResponse
import com.example.data.model.OpenWeatherGeocodingItem
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapService {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): OpenWeatherCurrentResponse

    @GET("data/2.5/forecast")
    suspend fun getFiveDayForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): OpenWeatherForecastResponse

    @GET("geo/1.0/direct")
    suspend fun geocodeCity(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<OpenWeatherGeocodingItem>
}
