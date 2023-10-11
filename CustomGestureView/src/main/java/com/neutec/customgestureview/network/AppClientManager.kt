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
    private var isDebug = false


    init {
        try{
            val name = UnitUtils.packageName + ".BuildConfig"
            isDebug = Class.forName(name)
                .getField("DEBUG")
                .getBoolean(null)
        }catch (e: Exception){
            Log.e("AppClientManager", "error = ${e.localizedMessage}")
        }

        apiRetrofit = Retrofit.Builder()
            .baseUrl(if (isDebug) testDomain else releaseDomain)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        Log.w("AppClientManager", "isDebug = $isDebug, domain = ${if (isDebug) testDomain else releaseDomain}")
    }

    companion object {
        private val manager = AppClientManager()

        val apiClient: Retrofit
            get() = manager.apiRetrofit
    }
}