package com.neutec.customgestureview.utility;

import java.util.HashSet;
import java.util.Set;

public class PreferenceContract {
    public static final String KEY_PATTERN_SHA1 = "pref_key_pattern_sha1";
    public static final String DEFAULT_PATTERN_SHA1 = null;

    public static final String KEY_DELETE_PATTERN_SHA1 = "pref_key_delete_pattern_sha1";
    public static final String DEFAULT_DELETE_PATTERN_SHA1 = null;

    public static final String KEY_APP_VERSION = "pref_key_app_version";
    public static final String DEFAULT_APP_VERSION = null;

    public static final String KEY_USER_ID = "pref_key_user_id";
    public static final String KEY_USER_ID_SET = "pref_key_user_id_set";
    public static final String KEY_IS_SHOW_SETTING_DIALOG_AFTER_UPDATE = "pref_key_is_show_setting_dialog_after_update";
    public static final String KEY_GESTURE_FIRST_ERROR_TIME = "pref_key_gesture_first_error_time";
    public static final String KEY_GESTURE_ERROR_COUNT = "pref_key_gesture_error_count";
    public static final String DEFAULT_USER_ID = null;
    public static final Boolean DEFAULT_IS_SHOW_SETTING_DIALOG_AFTER_UPDATE = true;
    public static final Set<String> DEFAULT_USER_ID_SET = new HashSet<>();

}