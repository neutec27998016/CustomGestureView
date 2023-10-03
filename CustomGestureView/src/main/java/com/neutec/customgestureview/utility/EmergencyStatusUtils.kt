package com.neutec.customgestureview.utility

import android.util.Log
import com.neutec.customgestureview.network.ApiService
import com.neutec.customgestureview.network.AppClientManager
import com.neutec.customgestureview.data.EmergencyData
import com.neutec.customgestureview.utility.UnitUtils.Companion.tag
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmergencyStatusUtils {

    fun checkEmergencyStatus() {
        val apiService = AppClientManager.apiClient.create(ApiService::class.java)
        apiService.getEmergencyData().enqueue(object : Callback<EmergencyData> {
            override fun onResponse(
                call: Call<EmergencyData>,
                response: Response<EmergencyData>
            ) {
                //當data陣列中emergency的值為1時，登出所有帳號

                val data = response.body()?.data

                Log.d(tag, "emergency = ${data?.emergency}")

                if (data?.emergency == 1) {
                    UnitUtils.forceLogoutUnit?.let { it() }
                }
            }

            override fun onFailure(call: Call<EmergencyData>, t: Throwable) {
                //介接失敗，不做任何動作
                Log.e(tag, "getEmergencyData error = ${t.localizedMessage}")
            }
        })
    }
}