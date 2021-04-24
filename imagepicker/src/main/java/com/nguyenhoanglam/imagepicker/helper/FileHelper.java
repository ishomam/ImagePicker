package com.nguyenhoanglam.imagepicker.helper;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;

public class FileHelper {
    private static final String TAG = FileHelper.class.getSimpleName();

    public static boolean isFileExisted(Context context, Uri uri) {
        if (uri == null) return false;
        boolean fileExists = false;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            inputStream.close();
            fileExists = true;
        } catch (Exception e) {
            Log.w(TAG, "File corresponding to the uri does not exist " + uri.toString());
        }
        return fileExists;
    }
}
