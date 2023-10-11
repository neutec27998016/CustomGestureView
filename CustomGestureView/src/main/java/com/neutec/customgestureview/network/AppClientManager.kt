package com.neutec.customgestureview.network

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.neutec.customgestureview.BuildConfig
import com.neutec.customgestureview.utility.UnitUtils


class AppClientManager {
    private val apiRetrofit: Retrofit
    private val okHttpClient = OkHttpClient()
    private val testDomain = "https://dev-emergency.gogocell.xyz/api/"
    private val releaseDomain = "https://emergency.gogocell.xyz/api/"


    init {
        apiRetrofit = Retrofit.Builder()
            .baseUrl(if (UnitUtils.isDebugmode) testDomain else releaseDomain)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        Log.w(
            "AppClientManager",
            "isDebugMode = ${UnitUtils.isDebugmode}, domain = ${if (UnitUtils.isDebugmode) testDomain else releaseDomain}"
        )
    }

    companion object {
        private val manager = AppClientManager()

        val apiClient: Retrofit
            get() = manager.apiRetrofit
    }
}