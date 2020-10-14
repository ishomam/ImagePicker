package com.nguyenhoanglam.imagepicker.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalOriEqualSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public HorizontalOriEqualSpaceItemDecoration(int spaceInPx) {
        this.space = spaceInPx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = 0;
        outRect.right = space;
        outRect.bottom = space;
        outRect.top = 0;
    }
}