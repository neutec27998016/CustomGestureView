package com.neutec.customgestureview.utility

class UnitUtils {
    companion object {
        const val tag = "CustomGestureView"
        //存放變數
        var appVersion = ""
        var logoutUnit: (() -> Unit)? = null
        var settingAccountUnit: (() -> Unit)? = null
        var forceLogoutUnit: (() -> Unit)? = null
        var isNowCheckEmergencyStatus: Boolean = false

        //2023/06/21新增
        var needCheckAirplaneMode = false //現階段不開啟檢查飛航模式版本
        var resetTime = 30 //預設為30分
    }

    fun getLogoutUnit(): (() -> Unit)? {
        return logoutUnit
    }

    fun setLogoutUnit(unit: () -> Unit) {
        logoutUnit = unit
    }

    fun getForceLogoutUnit(): (() -> Unit)? {
        return forceLogoutUnit
    }

    fun setForceLogoutUnit(unit: () -> Unit) {
        forceLogoutUnit = unit
    }

    fun getSettingAccountUnit(): (() -> Unit)? {
        return settingAccountUnit
    }

    fun setSettingAccountUnit(unit: () -> Unit) {
        settingAccountUnit = unit
    }

    fun setAppVersion(version: String) {
        appVersion = version
    }

    fun getAppVersion(): String {
        return appVersion
    }

    fun setIsNowCheckEmergencyStatus(status: Boolean) {
        isNowCheckEmergencyStatus = status
    }

    fun getIsNowCheckEmergencyStatus(): Boolean {
        return isNowCheckEmergencyStatus
    }

    //----------------------------------------------------------------//
    //2023/06/21新增 用於判斷是否需要檢查飛航模式
    fun setNeedCheckAirplaneMode(need: Boolean) {
        needCheckAirplaneMode = need
    }

    fun getNeedCheckAirplaneMode(): Boolean {
        return needCheckAirplaneMode
    }

    //2023/06/21新增 用於設定相隔多久時間重置
    fun setResetTime(time: Int) {
        resetTime = time
    }

    fun getResetTime(): Int {
        return resetTime
    }
    //----------------------------------------------------------------//
}