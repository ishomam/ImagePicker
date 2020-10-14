package com.nguyenhoanglam.imagepicker.listener;

import com.nguyenhoanglam.imagepicker.model.Image;

import java.util.List;

/**
 * Created by hoanglam on 8/18/17.
 */

public interface OnSelectedImagesChangeListener {
    void onRemoveImage(int position);
    void onAddImage(Image image, int position);
}
