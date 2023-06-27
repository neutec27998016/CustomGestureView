package com.neutec.customgestureview.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppClientManager {
    private val apiRetrofit: Retrofit
    private val okHttpClient = OkHttpClient()
    private val apiUrl = "https://emergency.gogocell.xyz/api/"

    init {
        apiRetrofit = Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    companion object {
        private val manager = AppClientManager()

        val apiClient: Retrofit
            get() = manager.apiRetrofit
    }
}