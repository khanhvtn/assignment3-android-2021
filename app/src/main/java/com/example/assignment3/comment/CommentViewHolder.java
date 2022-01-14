package com.example.assignment3.comment;


import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment3.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewHolder extends RecyclerView.ViewHolder {
    CircleImageView imagePoster;
    AppCompatTextView namePoster, commentTimestamp, textContent, countLike;
    AppCompatImageView imageContent;
    AppCompatImageButton btnLike;
    CardView imageContentCardView;

    public CommentViewHolder(View v) {
        super(v);
        this.imagePoster = v.findViewById(R.id.imagePoster);
        this.namePoster = v.findViewById(R.id.namePoster);
        this.commentTimestamp = v.findViewById(R.id.commentTimestamp);
        this.textContent = v.findViewById(R.id.textContent);
        this.countLike = v.findViewById(R.id.countLike);
        this.imageContent = v.findViewById(R.id.imageContent);
        this.btnLike = v.findViewById(R.id.btnLike);
        this.imageContentCardView = v.findViewById(R.id.imageContentCardView);
    }
}
