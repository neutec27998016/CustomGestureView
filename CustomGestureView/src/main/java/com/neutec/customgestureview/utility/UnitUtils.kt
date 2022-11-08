package com.neutec.customgestureview.utility

class UnitUtils {
    companion object {
        //存放變數
        var appVersion = ""
        var logoutUnit: (() -> Unit)? = null
        var settingAccountUnit: (() -> Unit)? = null
        var forceLogoutUnit: (() -> Unit)? = null

        var isNowCheckEmergencyStatus: Boolean = false
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
}