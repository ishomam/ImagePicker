package com.nguyenhoanglam.imagepicker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenhoanglam.imagepicker.R;
import com.nguyenhoanglam.imagepicker.listener.OnSelectedImagesChangeListener;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.common.BaseRecyclerViewAdapter;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImageLoader;

import java.util.ArrayList;
import java.util.List;


public class ImagesPreviewAdapter extends BaseRecyclerViewAdapter<ImagesPreviewAdapter.ImagePreviewViewHolder> {

    RecyclerView mRecyclerView;
    private Config config;
    private List<Image> selectedImages = new ArrayList<>();
    private static final int INVALID_POSITION = -1;

    private OnSelectedImagesChangeListener onSelectedImagesChangeListener;

    public ImagesPreviewAdapter(Context context, Config config, ImageLoader imageLoader,
                                List<Image> selectedImages) {
        super(context, imageLoader);
        this.config = config;

        if (selectedImages != null && !selectedImages.isEmpty()) {
            this.selectedImages.addAll(selectedImages);
        }
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

        getImageLoader().loadImage(selectedImages.get(position).getPath(), viewHolder.image);

        viewHolder.imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedImages.remove(position);
                notifyDataSetChanged();
                onSelectedImagesChangeListener.onRemoveImage(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return selectedImages.size();
    }


    public void removeAllSelected() {
        selectedImages.clear();
        notifyDataSetChanged();
    }

    public void removeImage(int position){
        if (selectedImages.get(position) != null) {
            selectedImages.remove(position);
            notifyDataSetChanged();
            if(getItemCount() > 0){
                mRecyclerView.scrollToPosition(getItemCount() - 1);
            }
        }
    }

    public void addImage(Image image, int position){
        selectedImages.add(image);
        notifyItemChanged(position);
        mRecyclerView.scrollToPosition(getItemCount() - 1);
    }

    public void setOnSelectedImagesChangeListener(OnSelectedImagesChangeListener
                                                          onSelectedImagesChangeListener) {
        this.onSelectedImagesChangeListener = onSelectedImagesChangeListener;
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
