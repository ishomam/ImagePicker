package com.nguyenhoanglam.imagepicker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class RecyclerViewConfigurator {

    private Builder builder;
    private Context context;
    public static final int HORIZONTAL_ORIENTATION = RecyclerView.HORIZONTAL;
    public static final int VERTICAL_ORIENTATION = RecyclerView.VERTICAL;

    public RecyclerViewConfigurator(Context context, RecyclerViewConfigurator.Builder builder) {
        this.context = context;
        this.builder = builder;
    }

    public void configure() {
        @SuppressLint("WrongConstant") GridLayoutManager layoutManager = new GridLayoutManager(
                context,
                builder.numOfColumnsOrRows,
                builder.orientation,
                builder.reverseLayout);

        builder.recyclerView.setLayoutManager(layoutManager);
        builder.recyclerView.setHasFixedSize(builder.hasFixedSize);
        builder.recyclerView.setAdapter(builder.adapter);

        if (builder.activateItemDecoration) {
            int spaceBetweenItemsInPx = dpToPx(context, builder.spaceBetweenItems);
            RecyclerView.ItemDecoration itemDecoration;
            if (builder.orientation == VERTICAL_ORIENTATION) {
                itemDecoration = new VerticalOriEqualSpaceItemDecoration(
                        builder.numOfColumnsOrRows, spaceBetweenItemsInPx, false);
            } else {
                itemDecoration = new HorizontalOriEqualSpaceItemDecoration(spaceBetweenItemsInPx);
            }
            builder.recyclerView.addItemDecoration(itemDecoration);
        }
    }

    private static int dpToPx(Context context, float dpSize){
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize,
                context.getResources().getDisplayMetrics()));
    }

    public static class Builder {

        private final Context context;
        private RecyclerView.Adapter adapter;
        private RecyclerView recyclerView;
        private float spaceBetweenItems = 3;
        private int numOfColumnsOrRows = 1;
        private int orientation = HORIZONTAL_ORIENTATION;
        private boolean hasFixedSize = true;
        private boolean reverseLayout = false;
        private boolean activateItemDecoration = false;

        public Builder(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
            this.recyclerView = recyclerView;
            this.adapter = adapter;
            this.context = recyclerView.getContext();
        }

        public Builder spaceBetweenItems(float spaceBetweenItems) {
            this.spaceBetweenItems = spaceBetweenItems;
            return this;
        }

        public Builder numOfColumnsOrRows(int numOfColumnsOrRows) {
            this.numOfColumnsOrRows = numOfColumnsOrRows;
            return this;
        }

        public Builder orientation(int orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder hasFixedSize(boolean hasFixedSize) {
            this.hasFixedSize = hasFixedSize;
            return this;
        }

        public Builder reverseLayout(boolean reverseLayout) {
            this.reverseLayout = reverseLayout;
            return this;
        }

        public Builder activateItemDecoration(boolean activateItemDecoration) {
            this.activateItemDecoration = activateItemDecoration;
            return this;
        }

        public RecyclerViewConfigurator build() {
            return new RecyclerViewConfigurator(context, this);
        }

    }

}
