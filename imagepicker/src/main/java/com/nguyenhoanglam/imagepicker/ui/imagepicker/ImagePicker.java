/*
 * Created by Nguyen Hoang Lam
 * Date: ${DATE}
 */

package com.nguyenhoanglam.imagepicker.ui.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import androidx.fragment.app.Fragment;

import com.nguyenhoanglam.imagepicker.R;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.util.ArrayList;

/**
 * Created by hoanglam on 8/4/16.
 */
public class ImagePicker {

    protected Config config;


    public ImagePicker(Builder builder) {
        config = builder.config;
    }

    public static Builder with(Activity activity) {
        return new ActivityBuilder(activity);
    }

    public static Builder with(Fragment fragment) {
        return new FragmentBuilder(fragment);
    }

    static class ActivityBuilder extends Builder {
        private Activity activity;

        public ActivityBuilder(Activity activity) {
            super(activity);
            this.activity = activity;
        }

        @Override
        public void start() {
            Intent intent = getIntent();
            int requestCode = config.getRequestCode() != 0 ? config.getRequestCode() : Config.RC_PICK_IMAGES;
            activity.startActivityForResult(intent, requestCode);
        }

        @Override
        public Intent getIntent() {
            Intent intent;
            intent = new Intent(activity, ImagePickerActivity.class);
            intent.putExtra(Config.EXTRA_CONFIG, config);
            return intent;
        }
    }

    static class FragmentBuilder extends Builder {
        private Fragment fragment;

        public FragmentBuilder(Fragment fragment) {
            super(fragment);
            this.fragment = fragment;
        }

        @Override
        public void start() {
            Intent intent = getIntent();
            int requestCode = config.getRequestCode() != 0 ? config.getRequestCode() : Config.RC_PICK_IMAGES;
            fragment.startActivityForResult(intent, requestCode);
        }

        @Override
        public Intent getIntent() {
            Intent intent;
            intent = new Intent(fragment.getActivity(), ImagePickerActivity.class);
            intent.putExtra(Config.EXTRA_CONFIG, config);
            return intent;
        }
    }

    public static abstract class Builder extends BaseBuilder {

        public Builder(Activity activity) {
            super(activity);
        }

        public Builder(Fragment fragment) {
            super(fragment.getContext());
        }

        public Builder setToolbarColor(String toolbarColor) {
            config.setToolbarColor(toolbarColor);
            return this;
        }

        public Builder setStatusBarColor(String statusBarColor) {
            config.setStatusBarColor(statusBarColor);
            return this;
        }

        public Builder setToolbarTextColor(String toolbarTextColor) {
            config.setToolbarTextColor(toolbarTextColor);
            return this;
        }

        public Builder setToolbarIconColor(String toolbarIconColor) {
            config.setToolbarIconColor(toolbarIconColor);
            return this;
        }

        public Builder setProgressBarColor(String progressBarColor) {
            config.setProgressBarColor(progressBarColor);
            return this;
        }

        public Builder setBackgroundColor(String backgroundColor) {
            config.setBackgroundColor(backgroundColor);
            return this;
        }

        public Builder setMultipleMode(boolean isMultipleMode) {
            config.setMultipleMode(isMultipleMode);
            return this;
        }

        public Builder setFolderMode(boolean isFolderMode) {
            config.setFolderMode(isFolderMode);
            return this;
        }

        public Builder setShowSelectedAsNumber(boolean showSelectedAsNumber) {
            config.setShowSelectedAsNumber(showSelectedAsNumber);
            return this;
        }

        public Builder setMaxSize(int maxSize) {
            config.setMaxSize(maxSize);
            return this;
        }

        public Builder setDoneTitle(String doneTitle) {
            config.setDoneTitle(doneTitle);
            return this;
        }

        public Builder setFolderTitle(String folderTitle) {
            config.setFolderTitle(folderTitle);
            return this;
        }

        public Builder setImageTitle(String imageTitle) {
            config.setImageTitle(imageTitle);
            return this;
        }

        public Builder setLimitMessage(String message) {
            config.setLimitMessage(message);
            return this;
        }

        public Builder setDirectoryName(String directoryName) {
            config.setDirectoryName(directoryName);
            return this;
        }

        public Builder setAlwaysShowDoneButton(boolean isAlwaysShowDoneButton) {
            config.setAlwaysShowDoneButton(isAlwaysShowDoneButton);
            return this;
        }

        public Builder setSelectedImages(ArrayList<Image> selectedImages) {
            config.setSelectedImages(selectedImages);
            return this;
        }

        public Builder setRequestCode(int requestCode) {
            config.setRequestCode(requestCode);
            return this;
        }

        public abstract void start();

        public abstract Intent getIntent();

    }

    public static abstract class BaseBuilder {

        protected Config config;

        public BaseBuilder(Context context) {
            this.config = new Config();

            Resources resources = context.getResources();
            config.setMultipleMode(true);
            config.setFolderMode(true);
            config.setShowSelectedAsNumber(false);
            config.setMaxSize(Config.MAX_SIZE);
            config.setDoneTitle(resources.getString(R.string.imagepicker_action_done));
            config.setFolderTitle(resources.getString(R.string.imagepicker_title_folder));
            config.setImageTitle(resources.getString(R.string.imagepicker_title_image));
            config.setLimitMessage(resources.getString(R.string.imagepicker_msg_limit_images));
            config.setDirectoryName(getDefaultDirectoryName(context));
            config.setAlwaysShowDoneButton(false);
            config.setSelectedImages(new ArrayList<Image>());
        }

        public String getDefaultDirectoryName(Context context) {
            final PackageManager pm = context.getPackageManager();
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo("your_package_name", 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            String directoryName = (String) (ai != null ? pm.getApplicationLabel(ai) : "Camera");
            return directoryName;
        }
    }

}

