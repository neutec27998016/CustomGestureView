package com.neutec.customgestureview.utility

open class LogoutUtils {
    var logoutUnit: (() -> Unit)? = null
    var logoutStatus: Int? = null

    fun setLogoutEvent(event: () -> Unit) {
        logoutUnit = event
    }

    fun getLogoutEvent(): (() -> Unit)? {
        return logoutUnit
    }

    fun setLogoutStatus(status: Int) {
        logoutStatus = status
    }

    fun getLogoutStatus(): Int {
        return logoutStatus ?: 0
    }
}