package com.nguyenhoanglam.imagepicker.ui.imagepicker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.nguyenhoanglam.imagepicker.listener.OnImageLoaderListener;
import com.nguyenhoanglam.imagepicker.model.Folder;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.common.BasePresenter;

import java.util.List;


/**
 * Created by hoanglam on 8/17/17.
 */

public class ImagePickerPresenter extends BasePresenter<ImagePickerView> {

    private final Context context;
    private ImageFileLoader imageLoader;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String TAG = ImagePickerPresenter.class.getSimpleName();

    public ImagePickerPresenter(Context context, ImageFileLoader imageLoader) {
        this.context = context;
        this.imageLoader = imageLoader;
    }

    public void abortLoading() {
        imageLoader.abortLoadImages();
    }

    public void loadImages(boolean isFolderMode) {
        if (!isViewAttached()) return;

        getView().showLoading(true);
        imageLoader.loadDeviceImages(isFolderMode, new OnImageLoaderListener() {
            @Override
            public void onImageLoaded(final List<Image> images, final List<Folder> folders) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            getView().showFetchCompleted(images, folders);
                            final boolean isEmpty = folders != null ? folders.isEmpty() : images.isEmpty();
                            if (isEmpty) {
                                getView().showEmpty();
                            } else {
                                getView().showLoading(false);
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailed(final Throwable throwable) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            getView().showError(throwable);
                        }
                    }
                });
            }
        });
    }

    public void onDoneSelectImages(List<Image> selectedImages) {
        // TODO, Note: originally non-existed files were excluded below, however non of the
        //  mentioned solutions is optimal. Therefore, the filtering is eliminated,
        //  in Phinsh non-existng photos will be excluded by checking loaded image for null

//        if (selectedImages != null && !selectedImages.isEmpty()) {
//            for (int i = 0; i < selectedImages.size(); i++) {
//                Image image = selectedImages.get(i);
//
////                // The following is not robust with ContentUris
////                File file = new File(image.getPath());
////                if (!file.exists()) {
////                    selectedImages.remove(i);
////                    i--;
////                }
////                // This is another robust solution but it's slow!
////                if (!FileHelper.isFileExisted(context, image.getUri())) {
////                    selectedImages.remove(i);
////                    i--;
////                }
//            }
//        }
        getView().finishPickImages(selectedImages);
    }
}
