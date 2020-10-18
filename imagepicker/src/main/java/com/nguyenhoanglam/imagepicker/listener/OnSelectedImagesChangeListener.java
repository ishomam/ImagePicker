package com.nguyenhoanglam.imagepicker.listener;

import com.nguyenhoanglam.imagepicker.model.Image;

import java.util.List;

public interface OnSelectedImagesChangeListener {
    void notifyImageRemoved(int position);
    void notifyImageAdded(int position);
    void onSelectionUpdate(List<Image> images);
}
