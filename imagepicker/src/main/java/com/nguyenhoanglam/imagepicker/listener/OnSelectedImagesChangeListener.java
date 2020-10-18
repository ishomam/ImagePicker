package com.nguyenhoanglam.imagepicker.listener;

import com.nguyenhoanglam.imagepicker.model.Image;

import java.util.List;

/**
 * Created by hoanglam on 8/18/17.
 */

public interface OnSelectedImagesChangeListener {
    void notifyImageRemoved(int position);
    void notifyImageAdded(int position);
}
