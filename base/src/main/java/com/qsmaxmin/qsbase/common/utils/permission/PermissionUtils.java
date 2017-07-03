package com.qsmaxmin.qsbase.common.utils.permission;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.qsmaxmin.qsbase.R;
import com.qsmaxmin.qsbase.common.log.L;
import com.qsmaxmin.qsbase.common.utils.QsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @CreateBy qsmaxmin
 * @Date 2017/3/7 12:36
 * @Description
 */

public class PermissionUtils {
    private static final String TAG = "PermissionUtils";
    private static PermissionUtils util;
    private        int             requestCode;
    private HashMap<String, PermissionBuilder> maps = new HashMap<>();

    private PermissionUtils() {
    }

    public static PermissionUtils getInstance() {
        if (util == null) {
            util = new PermissionUtils();
        }
        return util;
    }

    public PermissionBuilder createBuilder() {
        return new PermissionBuilder();
    }

    void startRequestPermission(PermissionBuilder builder) {
        if (builder == null) return;
        if (maps.containsValue(builder)) {
            L.e(TAG, "current permission is requesting, please don't request again....");
            return;
        }
        if (builder.getActivity() == null) {
            L.e(TAG, "activity can not be null, please setActivity()");
            return;
        }
        if (builder.getWantPermissionArr().size() == 0) {
            L.e(TAG, "you has not addWantPermission(String)");
            return;
        }
        ArrayList<String> unGrantedPermission = getUnGrantedPermissionArr(builder.getWantPermissionArr());
        if (unGrantedPermission.size() > 0) {
            if (builder.getActivity() != null) {
                requestCode++;
                L.i(TAG, "start request permission  requestCode=" + requestCode + "   wantPermission=" + unGrantedPermission.toString());
                builder.setRequestCode(requestCode);
                maps.put(String.valueOf(requestCode), builder);
                ActivityCompat.requestPermissions(builder.getActivity(), unGrantedPermission.toArray(new String[unGrantedPermission.size()]), requestCode);
            }
        } else {
            L.i(TAG, "all permission is granted....");
            if (builder.getListener() != null) {
                builder.getListener().onPermissionCallback(-1, true);
            }
        }
    }

    private ArrayList<String> getUnGrantedPermissionArr(List<String> list) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String permission : list) {
            if (ContextCompat.checkSelfPermission(QsHelper.getInstance().getApplication(), permission) != PackageManager.PERMISSION_GRANTED) {//用户未授权
                arrayList.add(permission);
            }
        }
        return arrayList;
    }


    /*------------------------------------- 以下是申请权限回调的数据解析 ---------------------------------------*/
    public void parsePermissionResultData(int requestCode, String[] permissions, int[] grantResults, Activity activity) {
        PermissionBuilder builder = maps.remove(String.valueOf(requestCode));
        if (builder == null) return;
        boolean grantedAll = true;
        ArrayList<String> unGrantedArr = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {//用户不同意，向用户展示该权限作用
                grantedAll = false;
                if (i < permissions.length && i >= 0) {
                    L.i(TAG, "user un granted permission:" + permissions[i]);
                    unGrantedArr.add(permissions[i]);
                }
            }
        }
        if (grantedAll) {
            L.i(TAG, "user granted all permission....");
            if (builder.getListener() != null) {
                builder.getListener().onPermissionCallback(builder.getRequestCode(), true);
            }

        } else {
            if (builder.getListener() != null) {
                builder.getListener().onPermissionCallback(builder.getRequestCode(), false);
            }
            if (builder.isShowCustomDialog()) {
                boolean shouldShowDialog = false;
                ArrayList<String> shouldShowDialogArr = new ArrayList<>();
                for (String unGrantedStr : unGrantedArr) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, unGrantedStr)) {
                        shouldShowDialog = true;
                        shouldShowDialogArr.add(unGrantedStr);
                    }
                }
                if (shouldShowDialog) {
                    showPermissionTipsDialog(shouldShowDialogArr);
                }
            }

        }
    }

    /**
     * 当系统提醒请求权限的对话框勾选不再提醒时，弹出的自定义对话框
     */
    private void showPermissionTipsDialog(ArrayList<String> showDialogPermission) {
        if (showDialogPermission == null || showDialogPermission.size() < 1) {
            return;
        }
        String message = getPermissionDialogMessage(showDialogPermission);
        if (TextUtils.isEmpty(message)) return;
        L.i(TAG, "勾选了不在提醒所以弹出自定义对话框：" + showDialogPermission.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(QsHelper.getInstance().getApplication());
        builder.setTitle(QsHelper.getInstance().getApplication().getString(android.R.string.dialog_alert_title))//
                .setMessage(message)//
                .setPositiveButton(QsHelper.getInstance().getApplication().getString(android.R.string.ok), new DialogInterface.OnClickListener() {//
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setNegativeButton(QsHelper.getInstance().getApplication().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

    private String getPermissionDialogMessage(ArrayList<String> permission) {
        StringBuilder stringbuilder = new StringBuilder();
        for (String str : permission) {
            switch (str) {
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    stringbuilder.append(QsHelper.getInstance().getApplication().getString(R.string.request_location_permission)).append("  ");
                    break;
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    stringbuilder.append(QsHelper.getInstance().getApplication().getString(R.string.request_read_external_storage_permission)).append("  ");
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    stringbuilder.append(QsHelper.getInstance().getApplication().getString(R.string.request_write_external_storage_permission)).append("  ");
                    break;
                case Manifest.permission.READ_CONTACTS:
                    stringbuilder.append(QsHelper.getInstance().getApplication().getString(R.string.request_constants_permission)).append("  ");
                    break;
                case Manifest.permission.CALL_PHONE:
                    stringbuilder.append(QsHelper.getInstance().getApplication().getString(R.string.request_call_permission)).append("  ");
                    break;
                case Manifest.permission.CAMERA:
                    stringbuilder.append(QsHelper.getInstance().getApplication().getString(R.string.request_camera_permission)).append("  ");
                    break;
                case Manifest.permission.RECORD_AUDIO:
                    stringbuilder.append(QsHelper.getInstance().getApplication().getString(R.string.request_record_audio_permission)).append("  ");
                    break;
                case Manifest.permission.READ_PHONE_STATE:
                    stringbuilder.append(QsHelper.getInstance().getApplication().getString(R.string.request_read_phone_state_permission)).append("  ");
                    break;
            }
        }
        return stringbuilder.length() < 1 ? null : stringbuilder.append(QsHelper.getInstance().getApplication().getString(R.string.request_permission_end)).toString();
    }
}