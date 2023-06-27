# CustomGestureView

### Gradle引入 

* 在需要使用的module下，找到`build.gradle`(TMessagesProj)檔案新增庫依賴:

```kotlin
android{
  ...
  dataBinding{
    enabled = true
  }
  ...
}

repositories {
    mavenCentral()
    google()
    maven { url 'https://jitpack.io' }
}

dependencies {
  implementation 'com.github.neutec27998016:CustomGestureView:1.0.0'
}
```
* 於`build.gradle`(TMessagesProj_App)內修改特定值
```
android{
  ...
  dataBinding{
    enabled = true
  }
  ...
  buildTypes {
    ...
    release {
      ...
      applicationIdSuffix ".taylorgray"
      ...
    }
    ...
  }
}

repositories {
    mavenCentral()
    google()
    maven { url 'https://jitpack.io' }
}
```

### LaunchActivity
* 引入
```java
import static org.telegram.messenger.BuildVars.BUILD_VERSION_STRING;
import com.neutec.customgestureview.activity.CustomGestureActivity;
import com.neutec.customgestureview.setting.UserData;
import com.neutec.customgestureview.utility.PatternLockUtils;
import com.neutec.customgestureview.utility.TimerUtils;
import com.neutec.customgestureview.utility.UnitUtils;
import kotlin.Unit;
import android.os.CountDownTimer;
```
    
* 宣告 `UnitUtils` 與 `CountDownTimer`
```java
private final UnitUtils unitUtils = new UnitUtils();  //宣告 UnitUtils 用以設定事件
private final CountDownTimer countDownTimer = new TimerUtils().getCountDownTimer();
```
    
* 於 `onCreate` 內設定事件
```java
unitUtils.setLogoutUnit(this::onLogoutUnit);
unitUtils.setForceLogoutUnit(this::onForceLogoutUnit);
unitUtils.setSettingAccountUnit(this::onSettingAccountUnit);
unitUtils.setAppVersion(BUILD_VERSION_STRING); //版本號
```
```java
private Unit onLogoutUnit() {
    List<String> userIdSet = PatternLockUtils.getUserIdSet(this);
    if (userIdSet.size() != 0) {
        boolean isLogout = false;
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (userIdSet.contains(String.valueOf(UserConfig.getInstance(a).getClientUserId()))) {
                switchToAccount(a, true);
                clearCache();
                MessagesController.getInstance(a).performLogout(1);
                isLogout = true;
            }
        }
        if (isLogout) {
            PatternLockUtils.setUserIdSet(new ArrayList<>(), this);
        }
    }
    return Unit.INSTANCE;
}
```
```java
private Unit onForceLogoutUnit() {
    List<String> userIdSet = PatternLockUtils.getUserIdSet(this);
    if (userIdSet.size() != 0) {
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            switchToAccount(a, true);
            MessagesController.getInstance(a).performLogout(1);
        }
        PatternLockUtils.setUserIdSet(new ArrayList<>(), this);
    }
  
    clearCache();
  
    //清除 app data
    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        am.clearApplicationUserData();
    }
  
    return Unit.INSTANCE;
}
```
```java
private Unit onSettingAccountUnit() {
    ArrayList<UserData> list = new ArrayList<>();
    for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
        TLRPC.User user = UserConfig.getInstance(a).getCurrentUser();
        if (user != null) {
            UserData data = new UserData();
            data.setId(String.valueOf(user.id));
            if (user.last_name != null) {
                data.setLastName(user.last_name);
            }
            if (user.first_name != null) {
                data.setFirstName(user.first_name);
            }
            list.add(data);
        }
    }
    PatternLockUtils.setActiveAccountList(list);
    return Unit.INSTANCE;
}
```
```java
private void clearCache() {
    AccountInstance accountInstance = AccountInstance.getInstance(UserConfig.selectedAccount);
    accountInstance.getFileLoader().cancelLoadAllFiles();
    accountInstance.getFileLoader().getFileLoaderQueue().postRunnable(() -> Utilities.globalQueue.postRunnable(() -> {
        Utilities.Callback<Float> updateProgress = t -> {
        };
        for (int a = 0; a < 8; a++) {
            int type = -1;
            int documentsMusicType = 0;
            if (a == 0) {
                type = FileLoader.MEDIA_DIR_IMAGE;
            } else if (a == 1) {
                type = FileLoader.MEDIA_DIR_VIDEO;
            } else if (a == 2) {
                type = FileLoader.MEDIA_DIR_DOCUMENT;
                documentsMusicType = 1;
            } else if (a == 3) {
                type = FileLoader.MEDIA_DIR_DOCUMENT;
                documentsMusicType = 2;
            } else if (a == 4) {
                type = FileLoader.MEDIA_DIR_AUDIO;
            } else if (a == 5) {
                type = 100;
            } else if (a == 6) {
                documentsMusicType = 5;
                type = FileLoader.MEDIA_DIR_CACHE;
            } else if (a == 7) {
                documentsMusicType = 4;
                type = FileLoader.MEDIA_DIR_CACHE;
            }
            if (type == -1) {
                continue;
            }
            File file;
            if (type == 100) {
                file = new File(FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE), "acache");
            } else {
                file = FileLoader.checkDirectory(type);
            }
            if (file != null) {
                CacheControlActivity.cleanDirJava(file.getAbsolutePath(), documentsMusicType, null, updateProgress);
            }
            if (type == 100) {
                file = FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE);
                if (file != null) {
                    CacheControlActivity.cleanDirJava(file.getAbsolutePath(), 3, null, updateProgress);
                }
            }
            if (type == FileLoader.MEDIA_DIR_IMAGE || type == FileLoader.MEDIA_DIR_VIDEO) {
                int publicDirectoryType;
                if (type == FileLoader.MEDIA_DIR_IMAGE) {
                    publicDirectoryType = FileLoader.MEDIA_DIR_IMAGE_PUBLIC;
                } else {
                    publicDirectoryType = FileLoader.MEDIA_DIR_VIDEO_PUBLIC;
                }
                file = FileLoader.checkDirectory(publicDirectoryType);

                if (file != null) {
                    CacheControlActivity.cleanDirJava(file.getAbsolutePath(), documentsMusicType, null, updateProgress);
                }
            }
            if (type == FileLoader.MEDIA_DIR_DOCUMENT) {
                file = FileLoader.checkDirectory(FileLoader.MEDIA_DIR_FILES);
                if (file != null) {
                    CacheControlActivity.cleanDirJava(file.getAbsolutePath(), documentsMusicType, null, updateProgress);
                }
            }
        }

        FileLoader.getInstance(UserConfig.selectedAccount).clearFilePaths();
        FileLoader.getInstance(UserConfig.selectedAccount).checkCurrentDownloadsFiles();
        ImageLoader.getInstance().clearMemory();

        accountInstance.getMediaDataController().ringtoneDataStore.checkRingtoneSoundsLoaded();
        MediaDataController.getInstance(UserConfig.selectedAccount).chekAllMedia(true);
    }));
}
```
* 於 `onResume` 內設定事件
```kotlin
if (PatternLockUtils.isNeedtoShowGestureLock) {
    startActivity(new Intent(this, CustomGestureActivity.class));
}
countDownTimer.start();
```
* 於 `onPause` 內設定事件
```kotlin
PatternLockUtils.isNeedtoShowGestureLock = true;
countDownTimer.cancel();
```
### LoginActivity
* 引入
```java
import static org.telegram.messenger.BuildVars.BUILD_VERSION_STRING;
import com.neutec.customgestureview.setting.SettingAccountDialog;
import com.neutec.customgestureview.setting.UserData;
import com.neutec.customgestureview.utility.PatternLockUtils;
```
* 於`needFinishActivity` 尾端新增 `checkAccount()`
```kotlin
private void checkAccount() {
    ArrayList<UserData> list = new ArrayList<>();
    for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
        TLRPC.User user = UserConfig.getInstance(a).getCurrentUser();
        if (user != null) {
            UserData data = new UserData();
            data.setId(String.valueOf(user.id));
            if (user.last_name != null) {
                data.setLastName(user.last_name);
            }
            if (user.first_name != null) {
                data.setFirstName(user.first_name);
            }
            list.add(data);
        }
    }
    PatternLockUtils.setActiveAccountList(list);

    SettingAccountDialog dialog = new SettingAccountDialog(LoginActivity.this.getParentActivity());
    dialog.show(false, BUILD_VERSION_STRING, list);
}
```
### AndroidManifest.xml
```
<application
  ...
  allowBackup="false"
  ...
>
```
### BuildVars
*替換 `APP_ID` & `APP_HASH`
```
public static int APP_ID = 1932276;
public static String APP_HASH = "9414555e3ec993309622880697baed7f";
```
### App Icon
* 於`mipmap-anydpi-v26`檔案夾內將'ic_launcher_round.xml'內容改為以下
```
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/icon_plane" />
</adaptive-icon>
```
### Google json
* 替換 `google json`，內容需含有 `org.telegram.messenger.taylorgray`

### release.keystore
* 複製`release.keystore` 至 `TMessagesProj/config`

### License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
