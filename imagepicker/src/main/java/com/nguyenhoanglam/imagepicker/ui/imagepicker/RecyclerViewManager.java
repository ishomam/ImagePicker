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
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Folder;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.model.ObservableList;
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

    private ObservableList<Image> selectedImages = new ObservableList<>(new ArrayList<Image>());

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

    public void setupAdapters(final OnImageClickListener imageClickListener, final OnFolderClickListener folderClickListener) {
        if (config.isMultipleMode() && !config.getSelectedImages().isEmpty()) {
            selectedImages = new ObservableList<>(config.getSelectedImages());
        }

        imagesPreviewAdapter = new ImagesPreviewAdapter(context, imageLoader, selectedImages);

        imageAdapter = new ImagePickerAdapter(context, config, imageLoader, selectedImages, imageClickListener);
        folderAdapter = new FolderPickerAdapter(context, imageLoader, new OnFolderClickListener() {
            @Override
            public void onFolderClick(Folder folder) {
                foldersState = recyclerView.getLayoutManager().onSaveInstanceState();
                folderClickListener.onFolderClick(folder);
            }
        });

        setupImagesPreviewAdapter();
        addListenersForSelectedImages();
    }

    private void addListenersForSelectedImages() {
        selectedImages.addListener(new ObservableList.ListChangeListener() {
            @Override
            public void onListChanged(List<?> list) {
            }
            @Override
            public void onBulkItemsChange() {
                imageAdapter.notifyDataSetChanged();
                imagesPreviewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onOneItemAdded() {
                imagesPreviewAdapter.notifyItemChanged(imagesPreviewAdapter.getItemCount() - 1);
                imagesPreviewRecyclerView.scrollToPosition(imagesPreviewAdapter.getItemCount() - 1);
                imageAdapter.notifyItemChanged(imageAdapter.getPositionOfLastSelectedPhoto());
            }
            @Override
            public void onOneItemRemoved(int index) {
                imagesPreviewAdapter.notifyDataSetChanged();
                imageAdapter.notifyDataSetChanged();
            }
        });
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

    public void setSelectedImagesChangeListener(ObservableList.ListChangeListener listChangeListener) {
        checkAdapterIsInitialized();
        selectedImages.addListener(listChangeListener);
    }

    public List<Image> getSelectedImages() {
        checkAdapterIsInitialized();
        return selectedImages.getList();
    }

    public void addSelectedImages(List<Image> images) {
        selectedImages.addAll(images);
    }

    private void checkAdapterIsInitialized() {
        if (imageAdapter == null) {
            throw new IllegalStateException("Must call setupAdapters first!");
        }
    }

    public boolean selectImage() {
        if (config.isMultipleMode()) {
            if (selectedImages.size() >= config.getMaxSize()) {
                return false;
            }
        } else {
            if (imageAdapter.getItemCount() > 0) {
                selectedImages.clear();
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
        imageAdapter.setData(images);
        setItemDecoration(imageColumns, recyclerView);
        recyclerView.setAdapter(imageAdapter);
        this.title = title;
        isShowingFolder = false;
    }

    public void removeSelectedImages() {
        selectedImages.clear();
    }

    public void setupImagesPreviewAdapter() {
        new RecyclerViewConfigurator.Builder(imagesPreviewRecyclerView, imagesPreviewAdapter)
                .activateItemDecoration(true)
                .numOfColumnsOrRows(1)
                .spaceBetweenItems(9)
                .orientation(RecyclerViewConfigurator.HORIZONTAL_ORIENTATION)
                .build()
                .configure();
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
        return config.isMultipleMode() && (config.isAlwaysShowDoneButton() || selectedImages.size() > 0);
    }
}
