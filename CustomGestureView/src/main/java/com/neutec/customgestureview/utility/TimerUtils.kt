package com.neutec.customgestureview.utility

import android.os.CountDownTimer
import com.neutec.customgestureview.utility.EmergencyStatusUtils

class TimerUtils {
    private var cdt = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            //do nothing
        }

        override fun onFinish() {
            this.start()
            EmergencyStatusUtils().checkEmergencyStatus()
        }
    }

    fun getCountDownTimer(): CountDownTimer {
        return cdt
    }
}