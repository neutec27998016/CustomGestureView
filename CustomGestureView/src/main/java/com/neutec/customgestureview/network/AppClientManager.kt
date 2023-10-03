package com.neutec.customgestureview.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.neutec.customgestureview.BuildConfig


class AppClientManager {
    private val apiRetrofit: Retrofit
    private val okHttpClient = OkHttpClient()

    init {
        apiRetrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_DOMAIN)
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