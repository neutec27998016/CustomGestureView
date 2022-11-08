package com.neutec.customgestureview.network

import com.neutec.customgestureview.data.EmergencyData
import com.neutec.customgestureview.data.VersionData
import retrofit2.Call
import retrofit2.http.GET


interface ApiService {
    @GET("version.txt")
    fun getVersionData(): Call<VersionData>

    //  I. 緊急狀態API網址：https://emergency.gogocell.xyz/api/emergency_status.php
    //      1. 測試緊急狀態方式：https://emergency.gogocell.xyz/api/emergency_status.php?emergency_test=1
    //      2. 測試介接404空網址方式：https://emergency.gogocell.xyz/api/emergency_status.php?emergency_test=404
    //      3. 測試介接500錯誤方式：https://emergency.gogocell.xyz/api/emergency_status.php?emergency_test=500
    //  II. 介接「緊急狀態API」時間點
    //      1.  一開啟app時，優先介接1次
    //      2. 處在app前景狀態下，Timer每60秒輪詢 1 次，永不停止
    //      3. 於背景返回前景時，優先介接1次
    //  III. 當data陣列中emergency的值為1時，登出所有帳號
    //  IV. 當data陣列中emergency的值為0時 或 介接失敗，不做任何動作
    @GET("emergency_status.php")
    fun getEmergencyData(): Call<EmergencyData>
}