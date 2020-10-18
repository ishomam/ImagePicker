package com.nguyenhoanglam.imagepicker.ui.imagepicker;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcelable;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenhoanglam.imagepicker.R;
import com.nguyenhoanglam.imagepicker.adapter.FolderPickerAdapter;
import com.nguyenhoanglam.imagepicker.adapter.ImagePickerAdapter;
import com.nguyenhoanglam.imagepicker.adapter.ImagesPreviewAdapter;
import com.nguyenhoanglam.imagepicker.listener.OnBackAction;
import com.nguyenhoanglam.imagepicker.listener.OnFolderClickListener;
import com.nguyenhoanglam.imagepicker.listener.OnImageClickListener;
import com.nguyenhoanglam.imagepicker.listener.OnImageSelectionListener;
import com.nguyenhoanglam.imagepicker.listener.OnSelectedImagesChangeListener;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Folder;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.utils.RecyclerViewConfigurator;
import com.nguyenhoanglam.imagepicker.widget.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hoanglam on 8/17/17.
 */

public class RecyclerViewManager {

    private Context context;
    private RecyclerView recyclerView;
    private RecyclerView imagesPreviewRecyclerView;
    private Config config;

    private GridLayoutManager layoutManager;
    private GridSpacingItemDecoration itemOffsetDecoration;

    private ImagesPreviewAdapter imagesPreviewAdapter;
    private ImagePickerAdapter imageAdapter;
    private FolderPickerAdapter folderAdapter;

    private List<Image> selectedImages = new ArrayList<>();

    private int imageColumns;
    private int folderColumns;

    private ImageLoader imageLoader;

    private Parcelable foldersState;
    private String title;
    private boolean isShowingFolder;


    public RecyclerViewManager(RecyclerView recyclerView, RecyclerView imagesPreviewRecyclerView,
                               Config config, int orientation) {
        this.recyclerView = recyclerView;
        this.imagesPreviewRecyclerView = imagesPreviewRecyclerView;
        this.config = config;
        context = recyclerView.getContext();
        changeOrientation(orientation);
        imageLoader = new ImageLoader();
        isShowingFolder = config.isFolderMode();
    }

    public void setupAdapters(OnImageClickListener imageClickListener, final OnFolderClickListener folderClickListener) {
        if (config.isMultipleMode() && !config.getSelectedImages().isEmpty()) {
            selectedImages = config.getSelectedImages();
        }

        imagesPreviewAdapter = new ImagesPreviewAdapter(context, config, imageLoader, selectedImages);

        imageAdapter = new ImagePickerAdapter(context, config, imageLoader, selectedImages, imageClickListener);
        folderAdapter = new FolderPickerAdapter(context, imageLoader, new OnFolderClickListener() {
            @Override
            public void onFolderClick(Folder folder) {
                foldersState = recyclerView.getLayoutManager().onSaveInstanceState();
                folderClickListener.onFolderClick(folder);
            }
        });

        setupImagesPreviewAdapter();
    }

    /**
     * Set item size, column size base on the screen orientation
     */
    public void changeOrientation(int orientation) {
        imageColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5;
        folderColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4;

        int columns = isShowingFolder ? folderColumns : imageColumns;
        layoutManager = new GridLayoutManager(context, columns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        setItemDecoration(columns, recyclerView);
    }

    private void setItemDecoration(int columns, RecyclerView recyclerView) {
        if (itemOffsetDecoration != null) {
            recyclerView.removeItemDecoration(itemOffsetDecoration);
        }
        itemOffsetDecoration = new GridSpacingItemDecoration(columns,
                context.getResources().getDimensionPixelSize(R.dimen.imagepicker_item_padding),
                false
        );
        recyclerView.addItemDecoration(itemOffsetDecoration);
        layoutManager.setSpanCount(columns);
    }


    public void setOnImageSelectionListener(OnImageSelectionListener imageSelectionListener) {
        checkAdapterIsInitialized();
        imageAdapter.setOnImageSelectionListener(imageSelectionListener);
    }

    public List<Image> getSelectedImages() {
        checkAdapterIsInitialized();
        return imageAdapter.getSelectedImages();
    }

    public void addSelectedImages(List<Image> images) {
        imageAdapter.addSelected(images);
    }

    private void checkAdapterIsInitialized() {
        if (imageAdapter == null) {
            throw new IllegalStateException("Must call setupAdapters first!");
        }
    }

    public boolean selectImage() {
        if (config.isMultipleMode()) {
            if (imageAdapter.getSelectedImages().size() >= config.getMaxSize()) {
                return false;
            }
        } else {
            if (imageAdapter.getItemCount() > 0) {
                imageAdapter.removeAllSelected();
            }
        }
        return true;
    }

    public void handleBack(OnBackAction action) {
        if (config.isFolderMode() && !isShowingFolder) {
            setFolderAdapter(null);
            action.onBackToFolder();
            return;
        }
        action.onFinishImagePicker();
    }

    public void setImageAdapter(List<Image> images, String title) {
        OnSelectedImagesChangeListener onSelectedImagesChangeListener =
                new OnSelectedImagesChangeListener() {
                    @Override
                    public void notifyImageAdded(int position) {
                        imagesPreviewAdapter.notifyImageAdded(position);
                    }
                    @Override
                    public void notifyImageRemoved(int position) {
                        imagesPreviewAdapter.notifyImageRemoved(position);
                    }
                };
        imageAdapter.setOnSelectedImagesChangeListener(onSelectedImagesChangeListener);

        imageAdapter.setData(images);
        setItemDecoration(imageColumns, recyclerView);
        recyclerView.setAdapter(imageAdapter);
        this.title = title;
        isShowingFolder = false;
    }

    public void removeSelectedImages() {
        imagesPreviewAdapter.removeAllSelected();
        imageAdapter.removeAllSelected();
    }

    public void setupImagesPreviewAdapter() {
        new RecyclerViewConfigurator.Builder(imagesPreviewRecyclerView, imagesPreviewAdapter)
                .activateItemDecoration(true)
                .numOfColumnsOrRows(1)
                .spaceBetweenItems(9)
                .orientation(RecyclerViewConfigurator.HORIZONTAL_ORIENTATION)
                .build()
                .configure();

        OnSelectedImagesChangeListener onSelectedImagesChangeListener =
                new OnSelectedImagesChangeListener() {
                    @Override
                    public void notifyImageRemoved(int position) {
                        imageAdapter.notifyImageRemoved(position);
                    }
                    @Override
                    public void notifyImageAdded(int position) {

                    }
                };
        imagesPreviewAdapter.setOnSelectedImagesChangeListener(onSelectedImagesChangeListener);
    }

    public void setFolderAdapter(List<Folder> folders) {
        folderAdapter.setData(folders);
        setItemDecoration(folderColumns, recyclerView);
        recyclerView.setAdapter(folderAdapter);
        isShowingFolder = true;

        if (foldersState != null) {
            layoutManager.setSpanCount(folderColumns);
            recyclerView.getLayoutManager().onRestoreInstanceState(foldersState);
        }
    }

    public String getTitle() {
        if (isShowingFolder) {
            return config.getFolderTitle();
        } else if (config.isFolderMode()) {
            return title;
        } else {
            return config.getImageTitle();
        }
    }

    public boolean isShowDoneButton() {
        return config.isMultipleMode() && (config.isAlwaysShowDoneButton() || imageAdapter.getSelectedImages().size() > 0);
    }
}
