package com.neutec.customgestureview.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppClientManager {
    private val retrofit: Retrofit
    private val emergencyUrlRetrofit: Retrofit
    private val okHttpClient = OkHttpClient()
    private val baseUrl = "https://dl.dropboxusercontent.com/s/dpbv72ljafn29hg/"
    private val emergencyUrl = "https://emergency.gogocell.xyz/api/"

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        emergencyUrlRetrofit = Retrofit.Builder()
            .baseUrl(emergencyUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    companion object {
        private val manager = AppClientManager()
        val client: Retrofit
            get() = manager.retrofit

        val emergencyClient: Retrofit
            get() = manager.emergencyUrlRetrofit
    }
}