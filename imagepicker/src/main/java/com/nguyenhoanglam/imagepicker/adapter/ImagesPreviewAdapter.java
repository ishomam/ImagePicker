package com.nguyenhoanglam.imagepicker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenhoanglam.imagepicker.R;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.model.ObservableList;
import com.nguyenhoanglam.imagepicker.ui.common.BaseRecyclerViewAdapter;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImageLoader;


public class ImagesPreviewAdapter extends BaseRecyclerViewAdapter<ImagesPreviewAdapter.ImagePreviewViewHolder> {

    RecyclerView mRecyclerView;
    private ObservableList<Image> selectedImages;

    public ImagesPreviewAdapter(Context context, ImageLoader imageLoader,
                                ObservableList<Image> selectedImages) {
        super(context, imageLoader);
        this.selectedImages = selectedImages;
    }

    @NonNull
    @Override
    public ImagePreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = getInflater().inflate(R.layout.imagepicker_item_image_preview,
                parent, false);
        return new ImagePreviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ImagePreviewViewHolder viewHolder, final int position) {

        getImageLoader().loadImage(selectedImages.get(position).getUri(), viewHolder.image);

        viewHolder.imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedImages.remove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedImages.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    static class ImagePreviewViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private ImageView imageDelete;

        public ImagePreviewViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_thumbnail);
            imageDelete = itemView.findViewById(R.id.image_delete);
        }

    }
}
