package com.neutec.customgestureview.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neutec.customgestureview.R
import com.neutec.customgestureview.data.VersionData
import com.neutec.customgestureview.data.VersionInfo
import com.neutec.customgestureview.network.ApiService
import com.neutec.customgestureview.network.AppClientManager
import com.neutec.customgestureview.utility.*
import com.neutec.customgestureview.utility.UnitUtils.Companion.resetTime
import com.neutec.customgestureview.utility.UnitUtils.Companion.tag
import com.neutec.customgestureview.view.GestureLockView
import me.zhanghai.android.patternlock.PatternUtils
import me.zhanghai.android.patternlock.PatternView.Cell
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GestureViewModel(application: Application) : AndroidViewModel(application) {
    enum class SettingType { COMMON_GESTURE_SETTING, COMMON_GESTURE_SETTING_AGAIN, SPECIAL_GESTURE_SETTING, SPECIAL_GESTURE_SETTING_AGAIN, LOCK, UNLOCK, UNLOCK_LOGOUT }

    private val TAG = "GestureViewModel"
    private var tmpCommonGestureLockResult: String = ""
    private var tmpSpecialGestureLockResult: String = ""
    private val gestureLength = 4
    private var firstErrorTime: Long = 0
    private var errorCount = 0

    var isCheckAppUpdateFinish = false
    var isAirplaneModeEverTurnedOn = false

    var commonGestureLock = MutableLiveData<String>()
    var specialGestureLock = MutableLiveData<String>()
    var nowType = MutableLiveData<SettingType>()
    var gestureErrorCount = MutableLiveData<Int>()
    var showErrorToast = MutableLiveData<Int>()
    var showUpdateDialog = MutableLiveData<VersionInfo>()

    fun getDescribeText(): Int {
        return when (nowType.value) {
            SettingType.COMMON_GESTURE_SETTING -> R.string.CommonGestureLookText
            SettingType.COMMON_GESTURE_SETTING_AGAIN -> R.string.CommonGestureLookAgainText
            SettingType.SPECIAL_GESTURE_SETTING -> R.string.SpecialGestureLookText
            SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.string.SpecialGestureLookAgainText
            else -> R.string.GestureLookText
        }
    }

    fun getDescribeTextColor(): Int {
        return when (nowType.value) {
            SettingType.SPECIAL_GESTURE_SETTING, SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.color.gesture_special_text
            else -> R.color.gesture_common_text
        }
    }

    fun getStepText(): Int {
        return when (nowType.value) {
            SettingType.COMMON_GESTURE_SETTING -> R.string.StepOne
            SettingType.COMMON_GESTURE_SETTING_AGAIN -> R.string.StepTwo
            SettingType.SPECIAL_GESTURE_SETTING -> R.string.StepThree
            SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.string.StepFour
            else -> R.string.Null
        }
    }

    fun getTitleText(): Int {
        return when (nowType.value) {
            SettingType.LOCK -> R.string.Gesture
            else -> R.string.GestureSetting
        }
    }

    fun getTitleTextColor(): Int {
        return when (nowType.value) {
            SettingType.SPECIAL_GESTURE_SETTING, SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.color.gesture_special_text
            else -> R.color.gesture_common_title_text
        }
    }

    fun getBackgroundColor(): Int {
        return when (nowType.value) {
            SettingType.SPECIAL_GESTURE_SETTING, SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> R.color.gesture_special_background
            else -> R.color.gesture_common_background
        }
    }

    fun checkGestureLockFromSharedPreferences(context: Context) {
        val specialGesture = PatternLockUtils.getDeletePattern(context)
        val commonGesture = PatternLockUtils.getPattern(context)

        nowType.value = if (commonGesture.isNullOrBlank() || specialGesture.isNullOrBlank()) {
            when {
                commonGesture.isNullOrBlank() -> {
                    SettingType.COMMON_GESTURE_SETTING
                }
                else -> {
                    SettingType.SPECIAL_GESTURE_SETTING
                }
            }
        } else {
            SettingType.LOCK
        }
    }

    fun checkIsNeedToShowSetting(context: Context, appVersion: String): Boolean {
        val version: String? = PreferenceUtils.getString(
            PreferenceContract.KEY_APP_VERSION,
            PreferenceContract.DEFAULT_APP_VERSION, context
        )
        return when {
            version == null && PatternLockUtils.getIsNeedToShowSettingDialog(context) == null -> true
            appVersion != version && PatternLockUtils.getIsNeedToShowSettingDialog(context) -> true
            else -> false
        }
    }

    fun isNeedToShowSettingDialog(context: Context, appVersion: String): Boolean {
        val version: String? = PreferenceUtils.getString(
            PreferenceContract.KEY_APP_VERSION,
            PreferenceContract.DEFAULT_APP_VERSION, context
        )
        return when {
            version == null -> true
            appVersion != version -> true
            else -> false
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun setCommonGestureLockFromSharedPreferences(context: Context, result: String) {
        PatternLockUtils.setPattern(getResultList(result), context)
        getCommonGestureLockFromSharedPreferences(context)
    }

    private fun getCommonGestureLockFromSharedPreferences(context: Context) {
        commonGestureLock.value = PatternLockUtils.getPattern(context)
    }

    @SuppressLint("CommitPrefEdits")
    fun setSpecialGestureLockFromSharedPreferences(context: Context, result: String) {
        PatternLockUtils.setDeletePattern(getResultList(result), context)
        getSpecialGestureLockFromSharedPreferences(context)
    }

    private fun getSpecialGestureLockFromSharedPreferences(context: Context) {
        specialGestureLock.value = PatternLockUtils.getDeletePattern(context)
    }


    fun redrawGesture() {
        when (nowType.value) {
            SettingType.COMMON_GESTURE_SETTING_AGAIN -> {
                tmpCommonGestureLockResult = ""
                nowType.value = SettingType.COMMON_GESTURE_SETTING
            }
            SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> {
                tmpSpecialGestureLockResult = ""
                nowType.value = SettingType.SPECIAL_GESTURE_SETTING
            }
            else -> {
                //do nothing
            }
        }
    }

    fun getGestureLockSuccess(context: Context, result: String?): Boolean {
        return when {
            result.isNullOrBlank() -> false
            else -> {
                when (nowType.value) {
                    SettingType.COMMON_GESTURE_SETTING -> {
                        if (result.length < gestureLength) {
                            showErrorToast(
                                context,
                                context.getString(R.string.GestureLengthNotEnough)
                            )
                            false
                        } else {
                            nowType.value = SettingType.COMMON_GESTURE_SETTING_AGAIN
                            tmpCommonGestureLockResult = result
                            true
                        }

                    }
                    SettingType.COMMON_GESTURE_SETTING_AGAIN -> {
                        if (result == tmpCommonGestureLockResult) {
                            setCommonGestureLockFromSharedPreferences(context, result)
                            true
                        } else {
                            false
                        }
                    }
                    SettingType.SPECIAL_GESTURE_SETTING -> {
                        if (result.length < gestureLength) {
                            showErrorToast(
                                context,
                                context.getString(R.string.GestureLengthNotEnough)
                            )
                            false
                        } else {
                            checkSpecialGestureLegal(context, result)
                        }
                    }
                    SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> {
                        if (result == tmpSpecialGestureLockResult) {
                            setSpecialGestureLockFromSharedPreferences(context, result)
                            true
                        } else {
                            false
                        }
                    }
                    else -> {
                        checkGestureIsCommonOrSpecial(context, result)
                    }
                }
            }
        }
    }

    fun gestureError(context: Context) {
        val firstErrorTime = PatternLockUtils.getGestureFirstErrorTime(context)
        var errorCount = PatternLockUtils.getGestureErrorCount(context)
        val currentTime = SystemClock.elapsedRealtime()
        when {
            UnitUtils.needCheckAirplaneMode && isAirplaneModeEverTurnedOn -> {
                //飛航模式曾經開啟
                errorCount++
                Log.w(tag, "gestureError: 飛航模式曾經開啟 ； errorCount = $errorCount")
            }

            currentTime < firstErrorTime -> {
                //手機被重啟過
                errorCount++
                PatternLockUtils.setGestureFirstErrorTime(currentTime, context)
                Log.w(tag, "gestureError: 手機被重啟過 ； errorCount = $errorCount")
            }

            currentTime > firstErrorTime -> {
                when {
                    currentTime - firstErrorTime > resetTime * 60 * 1000 -> {
                        //與上一次錯誤間隔超過設定
                        errorCount = 1
                        Log.w(tag, "gestureError: 與上一次錯誤間隔超過設定 ； errorCount = $errorCount")
                    }
                    else -> {
                        errorCount++
                        Log.w(tag, "gestureError: 與上一次錯誤間隔在設定內 ； errorCount = $errorCount")
                    }
                }
            }
        }

        when (errorCount) {
            1 -> {
                PatternLockUtils.setGestureFirstErrorTime(currentTime, context)
                PatternLockUtils.setGestureErrorCount(errorCount, context)
                showErrorToast.postValue(4)
            }
            5 -> {
                resetGestureError(context)
                UnitUtils.forceLogoutUnit?.invoke()
            }
            else -> {
                PatternLockUtils.setGestureErrorCount(errorCount, context)
                showErrorToast.postValue(5 - errorCount)
            }
        }
    }

    fun resetGestureError(context: Context) {
        PatternLockUtils.setGestureFirstErrorTime(0, context)
        PatternLockUtils.setGestureErrorCount(0, context)
    }

    fun checkAppVersion() {
        isCheckAppUpdateFinish = false
        val apiService = AppClientManager.apiClient.create(ApiService::class.java)
        apiService.getVersionData("android").enqueue(object : Callback<VersionData> {
            override fun onResponse(call: Call<VersionData>, response: Response<VersionData>) {
                val data = response.body()?.android
                if (PatternLockUtils.isNeedUpdate(
                        VersionInfo(
                            data?.force,
                            data?.version,
                            data?.downloadUrl
                        ), UnitUtils.appVersion
                    ) && !PatternLockUtils.isUpdateDialogShowed
                ) {
                    showUpdateDialog.postValue(
                        VersionInfo(
                            data?.force,
                            data?.version,
                            data?.downloadUrl
                        )
                    )
                } else {
                    isCheckAppUpdateFinish = true
                }
            }

            override fun onFailure(call: Call<VersionData>, t: Throwable) {
                isCheckAppUpdateFinish = true
                Log.e(tag, "getVersionData error = ${t.localizedMessage}")
            }
        })
    }

    private fun checkSpecialGestureLegal(context: Context, result: String): Boolean {
        tmpSpecialGestureLockResult = result

        val legal = !PatternLockUtils.isPatternSame(getResultList(result), context)
        if (!legal) {
            showErrorToast(context, context.getString(R.string.SpecialGestureCantSameAsCommon))
        } else {
            nowType.value = SettingType.SPECIAL_GESTURE_SETTING_AGAIN
        }
        return legal
    }

    private fun checkGestureIsCommonOrSpecial(context: Context, result: String): Boolean {
        val pattern = getResultList(result)
        var isCommonOrSpecial = false
        if (TextUtils.equals(
                PatternUtils.patternToSha1String(pattern),
                PatternLockUtils.getPattern(context)
            )
        ) {
            isCommonOrSpecial = true
            nowType.value = SettingType.UNLOCK
        } else if (TextUtils.equals(
                PatternUtils.patternToSha1String(pattern),
                PatternLockUtils.getDeletePattern(context)
            )
        ) {
            isCommonOrSpecial = true
            nowType.value = SettingType.UNLOCK_LOGOUT
        }

        return isCommonOrSpecial
    }

    private fun getResultList(result: String): List<Cell> {
        val array: Array<String> = result.toCharArray().map { it.toString() }.toTypedArray()
        val list: MutableList<Cell> = mutableListOf()
        array.forEach { string ->
            val row = string.toInt() / GestureLockView.sideLength
            val column = string.toInt() % GestureLockView.sideLength
            list.add(Cell.of(row, column))
        }
        return list
    }

    private fun showErrorToast(context: Context, error: String) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }
}