package com.neutec.customgestureviewlib

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neutec.customgestureview.activity.CustomGestureActivity
import com.neutec.customgestureview.setting.UserData
import com.neutec.customgestureview.utility.PatternLockUtils
import com.neutec.customgestureview.utility.UnitUtils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAccountList()
    }

    private fun setAccountList() {
        UnitUtils.logoutUnit = {
        }

        UnitUtils.settingAccountUnit = {
            val data = UserData()
            data.firstName = "Jeff"
            data.lastName = "Liu"
            data.id = "123456"

            val data1 = UserData()
            data1.firstName = "Jeff"
            data1.lastName = "Wang"
            data1.id = "654321"

            val list: ArrayList<UserData> = arrayListOf()
            list.add(data)
            list.add(data1)
            PatternLockUtils.setActiveAccountList(list)
        }

    }

    override fun onResume() {
        super.onResume()
        if (PatternLockUtils.isNeedtoShowGestureLock) {
            UnitUtils.appVersion = "8.1.1"
            startActivity(Intent(this, CustomGestureActivity::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
        PatternLockUtils.isNeedtoShowGestureLock = true
    }
}