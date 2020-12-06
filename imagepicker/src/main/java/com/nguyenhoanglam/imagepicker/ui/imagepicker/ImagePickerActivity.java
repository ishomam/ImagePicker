package com.nguyenhoanglam.imagepicker.ui.imagepicker;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.nguyenhoanglam.imagepicker.R;
import com.nguyenhoanglam.imagepicker.helper.LogHelper;
import com.nguyenhoanglam.imagepicker.helper.PermissionHelper;
import com.nguyenhoanglam.imagepicker.listener.OnBackAction;
import com.nguyenhoanglam.imagepicker.listener.OnFolderClickListener;
import com.nguyenhoanglam.imagepicker.listener.OnImageClickListener;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Folder;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.model.ObservableList;
import com.nguyenhoanglam.imagepicker.widget.ImagePickerToolbar;
import com.nguyenhoanglam.imagepicker.widget.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hoanglam on 7/31/16.
 */
public class ImagePickerActivity extends AppCompatActivity implements ImagePickerView {

    private ImagePickerToolbar toolbar;
    private RecyclerViewManager recyclerViewManager;
    private RecyclerView recyclerView;
    private RecyclerView imagesPreviewRecyclerView;
    private ProgressWheel progressWheel;
    private TextView noImageText;
    private TextView imagesCount;

    private Config config;
    private Handler handler;
    private ContentObserver observer;
    private ImagePickerPresenter presenter;
    private LogHelper logger = LogHelper.getInstance();


    private OnImageClickListener imageClickListener = new OnImageClickListener() {
        @Override
        public boolean onImageClick(View view, int position, boolean isSelected) {
            return recyclerViewManager.selectImage();
        }
    };

    private OnFolderClickListener folderClickListener = new OnFolderClickListener() {
        @Override
        public void onFolderClick(Folder folder) {
            setImageAdapter(folder.getImages(), folder.getFolderName());
        }
    };

    private View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    };

    private View.OnClickListener doneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onDone();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        config = intent.getParcelableExtra(Config.EXTRA_CONFIG);
        setContentView(R.layout.imagepicker_activity_picker);
        setupViews();
        setupComponents();
        setupToolbar();
        imagesCount.setText(String.format(getString(R.string.imagepicker_images_count),
                config.getSelectedImages().size()));
        getDataWithPermission();
    }

    private void setupViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        imagesPreviewRecyclerView = findViewById(R.id.imagesPreviewRecyclerView);
        progressWheel = findViewById(R.id.progressWheel);
        noImageText = findViewById(R.id.noImageText);
        imagesCount = findViewById(R.id.imagesCount);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(config.getStatusBarColor());
        }

        // To remove the blink (small fading) effect when using notifyItemChanged() in the Adapter
        SimpleItemAnimator animator = (SimpleItemAnimator) recyclerView.getItemAnimator();
        if (animator != null) {
            animator.setSupportsChangeAnimations(false);
        }


        progressWheel.setBarColor(config.getProgressBarColor());
        findViewById(R.id.container).setBackgroundColor(config.getBackgroundColor());

        progressWheel.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        noImageText.setVisibility(View.GONE);
    }

    private void setupComponents() {
        recyclerViewManager = new RecyclerViewManager(recyclerView, imagesPreviewRecyclerView,
                config, getResources().getConfiguration().orientation);
        recyclerViewManager.setupAdapters(imageClickListener, folderClickListener);
        recyclerViewManager.setSelectedImagesChangeListener(new ObservableList.ListChangeListener() {
            @Override
            public void onListChanged(List<?> images) {
                imagesCount.setText(String.format(getString(R.string.imagepicker_images_count),
                        images.size()));
                invalidateToolbar();
                if (!config.isMultipleMode() && !images.isEmpty()) {
                    onDone();
                }
            }
            @Override
            public void onBulkItemsChange() {
            }
            @Override
            public void onOneItemAdded() {
            }
            @Override
            public void onOneItemRemoved(int index) {
            }
        });

        presenter = new ImagePickerPresenter(new ImageFileLoader(this));
        presenter.attachView(this);
    }

    private void setupToolbar() {
        toolbar.config(config);
        toolbar.setOnBackClickListener(backClickListener);
        toolbar.setOnDoneClickListener(doneClickListener);
    }

    private void setImageAdapter(List<Image> images, String title) {
        recyclerViewManager.setImageAdapter(images, title);
        invalidateToolbar();
    }

    private void setFolderAdapter(List<Folder> folders) {
        recyclerViewManager.setFolderAdapter(folders);
        invalidateToolbar();
    }

    private void invalidateToolbar() {
        toolbar.setTitle(recyclerViewManager.getTitle());
        toolbar.showDoneButton(recyclerViewManager.isShowDoneButton());
    }

    private void onDone() {
        presenter.onDoneSelectImages(recyclerViewManager.getSelectedImages());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recyclerViewManager.changeOrientation(newConfig.orientation);
    }


    private void getDataWithPermission() {
        PermissionHelper.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                new PermissionHelper.PermissionAskListener() {
                    @Override
                    public void onPermissionGranted() {
                        // If permission granted, make sure that also the READ is granted..
                        PermissionHelper.checkPermission(ImagePickerActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                new PermissionHelper.PermissionAskListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        getData();
                                    }
                                });
                    }
                });
    }

    private void getData() {
        presenter.abortLoading();
        presenter.loadImages(config.isFolderMode());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PermissionHelper.RC_WRITE_EXTERNAL_STORAGE_PERMISSION: {
                PermissionHelper.handleRequestPermissionsResultForOnePermission(this,
                        grantResults, new PermissionHelper.RequestPermissionsResultListener() {
                            @Override
                            public void onPermissionDenied() {
                                logger.e("Permission not granted");
                            }
                            @Override
                            public void onPermissionGranted() {
                                logger.d("Write External permission granted");
                                getData();
                            }
                        });
            }
            case PermissionHelper.RC_READ_EXTERNAL_STORAGE_PERMISSION: {
                PermissionHelper.handleRequestPermissionsResultForOnePermission(this,
                        grantResults, new PermissionHelper.RequestPermissionsResultListener() {
                            @Override
                            public void onPermissionDenied() {
                                logger.e("Permission not granted");
                            }
                            @Override
                            public void onPermissionGranted() {
                                logger.d("Read External permission granted");
                                getData();
                            }
                        });
            }
            default: {
                logger.d("Got unexpected permission result: " + requestCode);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (handler == null) {
            handler = new Handler();
        }
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                getDataWithPermission();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (presenter != null) {
            presenter.abortLoading();
            presenter.detachView();
        }

        if (observer != null) {
            getContentResolver().unregisterContentObserver(observer);
            observer = null;
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    public void onBackPressed() {
        recyclerViewManager.handleBack(new OnBackAction() {
            @Override
            public void onBackToFolder() {
                invalidateToolbar();
            }

            @Override
            public void onFinishImagePicker() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    /**
     * MVP view methods
     */

    @Override
    public void showLoading(boolean isLoading) {
        progressWheel.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        noImageText.setVisibility(View.GONE);
    }

    @Override
    public void showFetchCompleted(List<Image> images, List<Folder> folders) {
        if (config.isFolderMode()) {
            setFolderAdapter(folders);
        } else {
            setImageAdapter(images, config.getImageTitle());
        }
        progressWheel.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        noImageText.setVisibility(View.GONE);
    }

    @Override
    public void showError(Throwable throwable) {
        String message = getString(R.string.imagepicker_error_unknown);
        if (throwable != null && throwable instanceof NullPointerException) {
            message = getString(R.string.imagepicker_error_images_not_exist);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        progressWheel.setVisibility(View.GONE);
    }

    @Override
    public void showEmpty() {
        progressWheel.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        noImageText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showCapturedImage(List<Image> images) {
        boolean shouldSelect = recyclerViewManager.selectImage();
        if (shouldSelect) {
            recyclerViewManager.addSelectedImages(images);
        }
        getDataWithPermission();
    }

    @Override
    public void finishPickImages(List<Image> images) {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(Config.EXTRA_IMAGES, (ArrayList<? extends Parcelable>) images);
        setResult(RESULT_OK, data);
        finish();
    }

    public void removeSelectedImages(View view) {
        recyclerViewManager.removeSelectedImages();
    }
}
