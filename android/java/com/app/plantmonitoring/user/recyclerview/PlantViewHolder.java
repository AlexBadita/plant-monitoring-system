package com.app.plantmonitoring.user.recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.plantmonitoring.R;

public class PlantViewHolder extends RecyclerView.ViewHolder{

    private TextView textView;
    private ImageView imageView;
    private CardView cardView;

    public PlantViewHolder(@NonNull View itemView) {
        super(itemView);

        textView = itemView.findViewById(R.id.item_text);
        imageView = itemView.findViewById(R.id.item_image);
        cardView = itemView.findViewById(R.id.item_container);
    }

    public TextView getTextView() {
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public CardView getCardView() {
        return cardView;
    }
}
