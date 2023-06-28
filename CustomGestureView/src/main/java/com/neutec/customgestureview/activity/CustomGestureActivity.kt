package com.neutec.customgestureview.activity

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.neutec.customgestureview.R
import com.neutec.customgestureview.setting.SettingAccountDialog
import com.neutec.customgestureview.utility.PatternLockUtils.*
import com.neutec.customgestureview.data.VersionInfo
import com.neutec.customgestureview.databinding.ActivityGestureLookBinding
import com.neutec.customgestureview.utility.EmergencyStatusUtils
import com.neutec.customgestureview.utility.UnitUtils
import com.neutec.customgestureview.view.listener.OnGestureLockListener
import com.neutec.customgestureview.view.painter.CirclePainter
import com.neutec.customgestureview.viewmodel.GestureViewModel


class CustomGestureActivity : AppCompatActivity(), OnGestureLockListener {
    private lateinit var binding: ActivityGestureLookBinding
    private var updateDialog: AlertDialog? = null
    private var forgotDialog: AlertDialog? = null
    private val gestureViewModel: GestureViewModel by lazy {
        ViewModelProvider(this)[GestureViewModel::class.java]
    }

    private var toast: Toast? = null

    //監聽飛航模式異動
    private val airplaneModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val isAirplaneModeOn =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Settings.System.getInt(
                        context.contentResolver,
                        Settings.Global.AIRPLANE_MODE_ON, 0
                    ) != 0
                } else {
                    false
                }
            if (isAirplaneModeOn) {
                gestureViewModel.isAirplaneModeEverTurnedOn = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar()
        binding = ActivityGestureLookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
        setGestureViewModel()
        gestureViewModel.checkGestureLockFromSharedPreferences(this)
        initView()
    }

    override fun onResume() {
        super.onResume()

        //2023/06/15 新增飛航模式檢查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Settings.System.getInt(
                    contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON, 0
                ) != 0
            ) {
                gestureViewModel.isAirplaneModeEverTurnedOn = true
            }
        }

        if (UnitUtils.appVersion.isNotBlank()) {
            if (gestureViewModel.nowType.value == GestureViewModel.SettingType.LOCK) {
                if (gestureViewModel.checkIsNeedToShowSetting(this, UnitUtils.appVersion)) {
                    setIsNeedToShowSettingDialog(false, this)
                    UnitUtils.settingAccountUnit?.let { it() }
                }
            } else {
                setIsNeedToShowSettingDialog(false, this)
            }
        }

        gestureViewModel.checkAppVersion()
        EmergencyStatusUtils().checkEmergencyStatus()
    }

    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(airplaneModeReceiver)
    }

    override fun onStarted() {
    }

    override fun onProgress(progress: String?) {
    }

    override fun onComplete(result: String?) {
        when (gestureViewModel.getGestureLockSuccess(this, result)) {
            true -> {
                binding.gestureView.clearView()
            }
            else -> {
                binding.gestureView.showErrorStatus(1000)
                if (gestureViewModel.nowType.value == GestureViewModel.SettingType.LOCK) {
                    gestureViewModel.gestureError()
                }
            }
        }
    }


    private fun showErrorToast(message: Int) {
        val errorString = "Retry Limit ($message/5)"
        toast?.cancel()
        toast = Toast.makeText(this, errorString, Toast.LENGTH_SHORT)
        toast?.show()
    }


    private fun showUpdateDialog(versionInfo: VersionInfo) {
        if (!isUpdateDialogShowed) {
            if (updateDialog == null) {
                val builder = AlertDialog.Builder(this@CustomGestureActivity)
                builder.setCancelable(false)
                builder.setMessage("App has a new version.\nDo you want to update?")
                if (versionInfo.force == "0") {
                    builder.setPositiveButton(
                        getString(R.string.Cancel)
                    ) { _, _ ->
                        isUpdateDialogShowed = true
                        gestureViewModel.isCheckAppUpdateFinish = true
                    }
                }
                builder.setNegativeButton(
                    getString(R.string.Update)
                ) { _, _ ->
                    val url = versionInfo.downloadUrl
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    finish()
                }
                updateDialog = builder.create()
            } else {
                if (updateDialog?.isShowing == true) {
                    updateDialog?.dismiss()
                }
            }
            updateDialog?.show()
        }
    }

    private fun showForgotDialog() {
        if (forgotDialog == null) {
            val builder = AlertDialog.Builder(this@CustomGestureActivity)
            builder.setCancelable(false)
            builder.setMessage("Do you want to reset pattern ?")
            builder.setPositiveButton(
                getString(R.string.Cancel)
            ) { _, _ -> }
            builder.setNegativeButton(
                getString(R.string.Reset)
            ) { _, _ ->
                clearPattern(this@CustomGestureActivity)
                UnitUtils.logoutUnit?.let { it() }
                finish()
            }
            forgotDialog = builder.create()
        } else {
            if (forgotDialog?.isShowing == true) {
                forgotDialog?.dismiss()
            }
        }
        forgotDialog?.show()
    }

    private fun setStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) // 確認取消半透明設置。
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE) // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) // 跟系統表示要渲染 system bar 背景。
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun setGestureViewModel() {
        gestureViewModel.showUpdateDialog.observe(this) {
            showUpdateDialog(it)
        }

        gestureViewModel.showErrorToast.observe(this) {
            showErrorToast(it)
        }

        gestureViewModel.commonGestureLock.observe(this) {
            if (it.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.GetCommonGestureError), Toast.LENGTH_SHORT)
                    .show()
            }
            gestureViewModel.checkGestureLockFromSharedPreferences(this)
        }

        gestureViewModel.specialGestureLock.observe(this) {
            if (it.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.GetSpecialGestureError), Toast.LENGTH_SHORT)
                    .show()
            } else {
                isNeedtoShowGestureLock = false
                finish()
            }
        }

        gestureViewModel.nowType.observe(this) {
            when (it) {
                GestureViewModel.SettingType.UNLOCK -> {
                    finishActivity()
                }
                GestureViewModel.SettingType.UNLOCK_LOGOUT -> {
//                    setUserIdSet(arrayListOf(), this)
                    UnitUtils.logoutUnit?.let { it() }
                    finishActivity()
                }
                else -> {
                    val anim = AlphaAnimation(1.0f, 0.0f)
                    anim.duration = 200
                    anim.repeatCount = 1
                    anim.repeatMode = Animation.REVERSE

                    anim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {}
                        override fun onAnimationStart(animation: Animation?) {
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                            binding.apply {
                                tvGestureDec.apply {
                                    text = getString(gestureViewModel.getDescribeText())
                                    setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            gestureViewModel.getDescribeTextColor()
                                        )
                                    )
                                }

                                tvStep.apply {
                                    text = getString(gestureViewModel.getStepText())
                                    setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            gestureViewModel.getDescribeTextColor()
                                        )
                                    )
                                }
                            }
                        }
                    })

                    binding.apply {
                        tvGestureDec.startAnimation(anim)
                        tvStep.startAnimation(anim)

                        tvRedraw.visibility = when (it) {
                            GestureViewModel.SettingType.COMMON_GESTURE_SETTING_AGAIN, GestureViewModel.SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> View.VISIBLE
                            else -> View.GONE
                        }

                        tvForgot.visibility = when (it) {
                            GestureViewModel.SettingType.LOCK -> View.VISIBLE
                            else -> View.GONE
                        }

                        with(rootView) {
                            setBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    gestureViewModel.getBackgroundColor()
                                )
                            )
                        }

                        with(gestureView) {
                            when (it) {
                                GestureViewModel.SettingType.SPECIAL_GESTURE_SETTING, GestureViewModel.SettingType.SPECIAL_GESTURE_SETTING_AGAIN -> {
                                    setNormalColor(Color.parseColor("#FFFFFF"))
                                    setPressedColor(Color.parseColor("#fdb915"))
                                }
                                else -> {
                                    setNormalColor(Color.parseColor("#808080"))
                                    setPressedColor(Color.parseColor("#2AABEE"))
                                }
                            }
                        }

                        with(tvGestureTitle) {
                            text = getString(gestureViewModel.getTitleText())
                            setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    gestureViewModel.getTitleTextColor()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.apply {
            gestureView.apply {
                setPainter(CirclePainter())
                setGestureLockListener(this@CustomGestureActivity)
            }

            tvGestureDec.apply {
                text = getString(gestureViewModel.getDescribeText())
            }

            tvRedraw.apply {
                setOnClickListener {
                    gestureViewModel.redrawGesture()
                }
            }

            tvForgot.apply {
                setOnClickListener {
                    showForgotDialog()
                }
            }

            tvGestureTitle.apply {
                setOnLongClickListener {
                    when (gestureViewModel.nowType.value) {
                        GestureViewModel.SettingType.LOCK -> {
                            UnitUtils.settingAccountUnit?.let { it() }
                            showSettingDialog()
                        }
                        else -> {
                            //do nothing
                        }
                    }

                    true
                }
            }
        }
    }

    private fun showSettingDialog() {
        val settingDialog = SettingAccountDialog(this@CustomGestureActivity)
        settingDialog.show(false, UnitUtils.appVersion, getActiveAccountList())
    }

    private fun finishActivity() {
        if (gestureViewModel.isCheckAppUpdateFinish) {
            isNeedtoShowGestureLock = false
            finish()
        }
    }
}