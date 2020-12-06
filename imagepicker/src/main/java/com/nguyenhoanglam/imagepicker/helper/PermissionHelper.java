package com.nguyenhoanglam.imagepicker.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nguyenhoanglam.imagepicker.R;


/**
 * Created by hoanglam on 8/21/17.
 */

public class PermissionHelper {

    public static final int RC_WRITE_EXTERNAL_STORAGE_PERMISSION = 101;
    public static final int RC_READ_EXTERNAL_STORAGE_PERMISSION = 102;
    public static final int RC_CAMERA_PERMISSION = 103;
    public static final int RC_OTHER_PERMISSION = 110;

    private static final String UNKNOWN_PERMISSION = "unknown";

    public static void checkPermission(Context context, String permission, PermissionAskListener listener) {
        // If API >= 23 and the permissions were not granted go further
        if (!hasPermission(context, permission)) {
            // shouldShowRequestPermissionRationale will be true when the user denied last time
            // so we should show him a dialog explaining we he needs to the permission
            if (shouldShowRequestPermissionRationale(context, permission)) {
//                requestPermission((Activity) context, permission);
                showDialogTellingUserWhyAndRequestPermissions(context, permission);
                // This time after this, when we do requestPermissions the user will have an
                // additional option "Never show again", if he clicked it, the further calls to
                // requestPermissions will automatically send a call back to onRequestPermissionsResult
                // as if he denied!
            } else {
                // User opened this for the first time so let's request his permission
                // If he selected "Never show again" before this will act as if he denied
                // automatically
                requestPermission((Activity) context, permission);
            }
        } else {
            listener.onPermissionGranted();
        }
    }

    private static void requestPermission(@NonNull Activity activity, @NonNull String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, getRequestCode(permission));
    }

    private static int getRequestCode(String permission){
        int rc;
        switch(permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                rc = RC_WRITE_EXTERNAL_STORAGE_PERMISSION;
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                rc = RC_READ_EXTERNAL_STORAGE_PERMISSION;
                break;
            case Manifest.permission.CAMERA:
                rc = RC_CAMERA_PERMISSION;
                break;
            default:
                rc = RC_OTHER_PERMISSION;
                break;
        }
        return rc;
    }

    private static String getPermissionFromRequestCode(int requestCode){
        String permission;
        switch(requestCode) {
            case RC_WRITE_EXTERNAL_STORAGE_PERMISSION:
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
            case RC_READ_EXTERNAL_STORAGE_PERMISSION:
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                break;
            case RC_CAMERA_PERMISSION:
                permission = Manifest.permission.CAMERA;
                break;
            default:
                permission = UNKNOWN_PERMISSION;
                break;
        }
        return permission;
    }

    private static void showDialogTellingUserWhyAndRequestPermissions(final Context context,
                                                                      final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.imagepicker_permission_needed);
        alertBuilder.setMessage(R.string.imagepicker_please_grant_permission);
        alertBuilder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        requestPermission((Activity) context, permission);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    public static void handleRequestPermissionsResultForOnePermission(Context context,
                                                                      int[] grantResults,
                                                                      int requestCode,
                                                                      RequestPermissionsResultListener
                                                                              requestPermissionsResultListener,
                                                                      OpenSettingDialogListener
                                                                              openSettingDialogListener) {
        if (grantResults != null) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionsResultListener.onPermissionGranted();
                return;
            }
        }
        // If user denied show him dialog which may take him to settings (this is especially useful
        // when the user clicked on "Never ask again" which makes the "requestPermissions" call
        // returns PackageManager.PERMISSION_DENIED always!)
        String permission = getPermissionFromRequestCode(requestCode);
        showDialogWithOpenSettingsOption(context, permission, openSettingDialogListener);
        requestPermissionsResultListener.onPermissionDenied();
    }

    public static void showDialogWithOpenSettingsOption(final Context context,
                                                        final String permission,
                                                        final OpenSettingDialogListener openSettingDialogListener) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(R.string.imagepicker_permission_needed);
        alertBuilder.setMessage(R.string.imagepicker_enable_permission_from_app_settings);
        alertBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                openSettingDialogListener.onCancelOrOkClick();
            }
        });
        alertBuilder.setNeutralButton(R.string.imagepicker_open_settings,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openAppSettings((Activity) context);
                        openSettingDialogListener.onCancelOrOkClick();
                    }
                });
        if (!permission.equals(UNKNOWN_PERMISSION)){
            alertBuilder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermission((Activity) context, permission);
                        }
                    });
        }
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    public static void openAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        activity.startActivity(intent);
    }

    public static String[] asArray(@NonNull String... permissions) {
        if (permissions.length == 0) {
            throw new IllegalArgumentException("There is no given permission");
        }

        final String[] dest = new String[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            dest[i] = permissions[i];
        }
        return dest;
    }

    public static boolean hasPermission(Context context, String permission) {
        if (shouldAskPermission()) {
            return permissionHasBeenGranted(context, permission);
        }
        return true;
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        if (shouldAskPermission()) {
            for (String permission : permissions) {
                if (!permissionHasBeenGranted(context, permission)) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressLint("NewApi")
    public static void requestAllPermissions(@NonNull Activity activity, @NonNull String[] permissions, int requestCode) {
        if (shouldAskPermission()) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    private static boolean permissionHasBeenGranted(Context context, String permission) {
        return hasBeenGranted(ContextCompat.checkSelfPermission(context, permission));
    }

    public static boolean hasBeenGranted(int grantResult) {
        return grantResult == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasBeenGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (!hasBeenGranted(result)) {
                return false;
            }
        }
        return true;
    }

    private static boolean shouldAskPermission() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    public static boolean shouldShowRequestPermissionRationale(Context context, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission);
    }

    public interface PermissionAskListener {
        void onPermissionGranted();
    }

    public interface RequestPermissionsResultListener {
        void onPermissionDenied();
        void onPermissionGranted();
    }

    public interface OpenSettingDialogListener {
        void onCancelOrOkClick();
    }

}
