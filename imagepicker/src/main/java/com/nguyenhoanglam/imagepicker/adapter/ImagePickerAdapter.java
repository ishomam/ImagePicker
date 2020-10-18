package com.nguyenhoanglam.imagepicker.adapter;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenhoanglam.imagepicker.R;
import com.nguyenhoanglam.imagepicker.helper.ImageHelper;
import com.nguyenhoanglam.imagepicker.listener.OnImageClickListener;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.model.ObservableList;
import com.nguyenhoanglam.imagepicker.ui.common.BaseRecyclerViewAdapter;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImageLoader;

import java.util.ArrayList;
import java.util.List;


public class ImagePickerAdapter extends BaseRecyclerViewAdapter<ImagePickerAdapter.ImageViewHolder> {

    private Config config;
    private List<Image> images = new ArrayList<>();
    private ObservableList<Image> selectedImages;
    private OnImageClickListener itemClickListener;
    private int positionOfLastSelectedPhoto;

    public ImagePickerAdapter(Context context, Config config, ImageLoader imageLoader,
                              ObservableList<Image> selectedImages, OnImageClickListener itemClickListener) {
        super(context, imageLoader);
        this.config = config;
        this.itemClickListener = itemClickListener;
        this.selectedImages = selectedImages;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = getInflater().inflate(R.layout.imagepicker_item_image, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder viewHolder, final int position) {

        final Image image = images.get(position);
        final int selectedPosition = getSelectedPosition(image);
        final boolean isSelected = selectedPosition != -1;

        getImageLoader().loadImage(image.getPath(), viewHolder.image);

        viewHolder.gifIndicator.setVisibility(ImageHelper.isGifFormat(image) ? View.VISIBLE : View.GONE);

        // Added by Homam
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isSelected) {
                viewHolder.image.setForeground(getContext()
                        .getDrawable(R.drawable.imagepicker_selected_image_border));
            } else {
                viewHolder.image.setForeground(null);
            }
        }

        viewHolder.selectedIcon.setVisibility(isSelected && !config.isShowSelectedAsNumber() ? View.VISIBLE : View.GONE);
        viewHolder.selectedNumber.setVisibility(isSelected && config.isShowSelectedAsNumber() ? View.VISIBLE : View.GONE);
        if (viewHolder.selectedNumber.getVisibility() == View.VISIBLE) {
            viewHolder.selectedNumber.setText((selectedPosition + 1) + "");
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean shouldSelect = itemClickListener.onImageClick(view,
                        viewHolder.getAdapterPosition(), !isSelected);
                if (isSelected) {
                    removeSelected(image);
                } else if (shouldSelect) {
                    positionOfLastSelectedPhoto = position;
                    selectedImages.add(image);
                } else {
                    String message = String.format(config.getLimitMessage(), config.getMaxSize());
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int getSelectedPosition(Image image) {
        for (int i = 0; i < selectedImages.size(); i++) {
            Image selectedImage = selectedImages.get(i);
            if (selectedImage.getPath().equals(image.getPath())) {
                return i;
            }
        }
        return -1;
    }

    public int getPositionOfLastSelectedPhoto(){
        return positionOfLastSelectedPhoto;
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setData(List<Image> images) {
        if (images != null) {
            this.images.clear();
            this.images.addAll(images);
        }
        notifyDataSetChanged();
    }

    public void removeSelected(Image image) {
        for (int i = 0; i < selectedImages.size(); i++) {
            if (selectedImages.get(i).getId() == image.getId()) {
                selectedImages.remove(i);
                break;
            }
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private ImageView selectedIcon;
        private TextView selectedNumber;
        private View gifIndicator;

        public ImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_thumbnail);
            selectedIcon = itemView.findViewById(R.id.image_selected_icon);
            selectedNumber = itemView.findViewById(R.id.text_selected_number);
            gifIndicator = itemView.findViewById(R.id.gif_indicator);
        }

    }
}
