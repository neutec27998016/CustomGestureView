package com.neutec.customgestureview.utility;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.neutec.customgestureview.data.VersionInfo;
import com.neutec.customgestureview.setting.UserData;
import com.neutec.customgestureview.utility.PreferenceContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import kotlin.Unit;
import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

public class PatternLockUtils {
    public static boolean isNeedtoShowGestureLock = true;
    public static final int REQUEST_CODE_CONFIRM_PATTERN = 1214;

    public static final int PATTERN_STATUS_WRONG = 0;
    public static final int PATTERN_STATUS_UNLOCK = 1;
    public static final int PATTERN_STATUS_DELETE = 2;

    public static final int CONFIRM_PATTERN_LOCK = 998;
    public static final int CLOSE_APP = 999;

    public static Consumer<Integer> mSpecialGestureEvent;

    private static int mPatternStatus;

    private static VersionInfo mVersionInfo;

    public static Boolean isUpdateDialogShowed = false;

    public static ArrayList<UserData> activeAccountList = new ArrayList<>();

    private PatternLockUtils() {
    }

    public static VersionInfo getVersionInfo() {
        return mVersionInfo;
    }

    public static void setVersionInfo(VersionInfo versionInfo) {
        mVersionInfo = versionInfo;
    }

    public static void setPatternStatus(int patternStatus) {
        mPatternStatus = patternStatus;
    }

    public static int getPatternStatus() {
        return mPatternStatus;
    }

    public static void setPattern(List<PatternView.Cell> pattern, Context context) {
        PreferenceUtils.putString(PreferenceContract.KEY_PATTERN_SHA1,
                PatternUtils.patternToSha1String(pattern), context);
    }

    public static String getPattern(Context context) {
        return PreferenceUtils.getString(PreferenceContract.KEY_PATTERN_SHA1,
                PreferenceContract.DEFAULT_PATTERN_SHA1, context);
    }

    public static void setDeletePattern(List<PatternView.Cell> pattern, Context context) {
        PreferenceUtils.putString(PreferenceContract.KEY_DELETE_PATTERN_SHA1,
                PatternUtils.patternToSha1String(pattern), context);
    }

    public static String getDeletePattern(Context context) {
        return PreferenceUtils.getString(PreferenceContract.KEY_DELETE_PATTERN_SHA1,
                PreferenceContract.DEFAULT_DELETE_PATTERN_SHA1, context);
    }

    public static void setIsNeedToShowSettingDialog(Boolean isNeed, Context context) {
        PreferenceUtils.putBoolean(PreferenceContract.KEY_IS_SHOW_SETTING_DIALOG_AFTER_UPDATE,
                isNeed, context);
    }

    public static Boolean getIsNeedToShowSettingDialog(Context context) {
        return PreferenceUtils.getBoolean(PreferenceContract.KEY_IS_SHOW_SETTING_DIALOG_AFTER_UPDATE,
                PreferenceContract.DEFAULT_IS_SHOW_SETTING_DIALOG_AFTER_UPDATE, context);
    }

    public static void setActiveAccountList(ArrayList<UserData> list) {
        activeAccountList = list;
    }

    public static ArrayList<UserData> getActiveAccountList() {
        return activeAccountList;
    }

    public static void setUserIdSet(ArrayList<String> userIdList, Context context) {
        PreferenceUtils.putString(PreferenceContract.KEY_USER_ID,
                userIdList.toString(), context);
    }

    public static List<String> getUserIdSet(Context context) {
        getOldVersionSetStringAndClear(context);
        return convertStringToList(PreferenceUtils.getString(PreferenceContract.KEY_USER_ID,
                PreferenceContract.DEFAULT_USER_ID, context));
    }

    public static List<String> convertStringToList(String stringList) {
        if (stringList != null && !stringList.isEmpty()) {
            return Arrays.asList(stringList.replaceAll("\\[", "").replaceAll("\\]", "").split(", "));
        } else {
            return new ArrayList<>();
        }
    }

    private static void getOldVersionSetStringAndClear(Context context) {
        if (PreferenceUtils.containKey(PreferenceContract.KEY_USER_ID_SET, context)) {
            Set<String> stringSet = PreferenceUtils.getStringSet(PreferenceContract.KEY_USER_ID_SET,
                    PreferenceContract.DEFAULT_USER_ID_SET, context);
            if (stringSet.size() > 0) {
                String string = Arrays.toString(Arrays.copyOf(stringSet.toArray(), stringSet.size(),
                        String[].class));

                PreferenceUtils.putString(PreferenceContract.KEY_USER_ID,
                        string, context);
            }
            PreferenceUtils.remove(PreferenceContract.KEY_USER_ID_SET, context);
        }
    }

    public static boolean isPatternSame(List<PatternView.Cell> pattern, Context context) {
        return TextUtils.equals(PatternUtils.patternToSha1String(pattern), getPattern(context));
    }

    public static boolean isNeedUpdate(VersionInfo versionInfo, String version) {
        if (versionInfo == null) {
            return false;
        } else {
            if (!version.isEmpty()) {
                String serverVersion = versionInfo.getVersion();
                return compareVersions(serverVersion, version);
            } else {
                return false;
            }
        }
    }

    @Deprecated
    //2023/06/21 改用compareVersions
    private static int doNumberFormat(String text) {
        text = text.replace(".", "");
        if (text.length() > 5) {
            return Integer.parseInt(text);
        }
        String pattern = "%-5s";
        return Integer.parseInt(String.format(pattern, text).replace(" ", "0"));
    }

    public static boolean compareVersions(String apiVersion, String nowAppVersion) {
        String[] v1 = apiVersion.split("\\.");
        String[] v2 = nowAppVersion.split("\\.");

        int maxLength = Math.max(v1.length, v2.length);

        for (int i = 0; i < maxLength; i++) {
            int v1Value = i < v1.length ? Integer.parseInt(v1[i]) : 0;
            int v2Value = i < v2.length ? Integer.parseInt(v2[i]) : 0;

            if (v1Value > v2Value) {
                return true;
            } else if (v1Value < v2Value) {
                return false;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setSpecialGestureEvent(Consumer<Integer> event) {
        mSpecialGestureEvent = event;
    }

    public static Consumer<Integer> getSpecialGestureEvent() {
        return mSpecialGestureEvent;
    }

    public static void clearPattern(Context context) {
        PreferenceUtils.remove(PreferenceContract.KEY_PATTERN_SHA1, context);
        PreferenceUtils.remove(PreferenceContract.KEY_DELETE_PATTERN_SHA1, context);
    }

    public static <ActivityType extends Activity & OnConfirmPatternResultListener> boolean
    checkConfirmPatternResult(ActivityType activity, int requestCode, int resultCode) {
        if (requestCode == REQUEST_CODE_CONFIRM_PATTERN) {
            activity.onConfirmPatternResult(resultCode == Activity.RESULT_OK);
            return true;
        } else {
            return false;
        }
    }

    public interface OnConfirmPatternResultListener {
        void onConfirmPatternResult(boolean successful);
    }
}