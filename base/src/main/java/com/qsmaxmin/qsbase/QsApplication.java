package com.qsmaxmin.qsbase;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;

import com.qsmaxmin.qsbase.common.http.HttpBuilder;
import com.qsmaxmin.qsbase.common.log.L;
import com.qsmaxmin.qsbase.common.utils.QsHelper;
import com.qsmaxmin.qsbase.common.widget.dialog.QsProgressDialog;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import okhttp3.Response;

/**
 * @CreateBy qsmaxmin
 * @Date 2017/6/20 16:40
 * @Description
 */

public abstract class QsApplication extends Application {
    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) return;
        if (isMemoryWatcherOpen()) {
            refWatcher = LeakCanary.install(this);
            L.i("Application", "memory watcher is open, so call LeakCanary.install....");
        }
        if (isLogOpen()) L.init(true);
        QsHelper.getInstance().init(this);

    }

    public abstract boolean isLogOpen();

    public abstract void initHttpAdapter(HttpBuilder builder);

    public void onActivityCreate(Activity activity) {
    }

    public void onActivityStart(Activity activity) {
    }

    public void onActivityResume(Activity activity) {
    }

    public void onActivityPause(Activity activity) {
    }

    public void onActivityStop(Activity activity) {
    }

    public void onActivityDestroy(Activity activity) {
    }

    /**
     * 公共progressDialog
     */
    public QsProgressDialog getCommonProgressDialog() {
        return null;
    }

    public @LayoutRes int loadingLayoutId() {
        return 0;
    }

    public @LayoutRes int emptyLayoutId() {
        return 0;
    }

    public @LayoutRes int errorLayoutId() {
        return 0;
    }

    public @DrawableRes int defaultImageHolder() {
        return 0;
    }

    public void onCommonHttpResponse(Response response) {
    }

    /**
     * 获取当前进程名
     */
    public String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return processName;
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
                break;
            }
        }
        return processName;
    }

    public boolean isMainProcess() {
        return getPackageName().equals(getCurrentProcessName());
    }

    public boolean isCurrentProcess(String processName) {
        return getCurrentProcessName().equals(processName);
    }

    public boolean isMemoryWatcherOpen() {
        return false;
    }

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }
}
