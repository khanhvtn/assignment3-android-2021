package com.example.assignment3.homescreen;


import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment3.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewHolder extends RecyclerView.ViewHolder {
    CircleImageView imagePoster;
    AppCompatTextView namePoster,postTimestamp, textContent, txtLikeCount, txtCommentCount;
    AppCompatImageView imageContent;
    AppCompatImageButton btnLike,btnComment, btnMoreOptions;
    CardView imageContentCardView;
    public PostViewHolder(View v) {
        super(v);
        this.imagePoster = v.findViewById(R.id.imagePoster);
        this.namePoster = v.findViewById(R.id.namePoster);
        this.postTimestamp = v.findViewById(R.id.postTimestamp);
        this.textContent = v.findViewById(R.id.textContent);
        this.txtLikeCount = v.findViewById(R.id.txtLikeCount);
        this.txtCommentCount = v.findViewById(R.id.txtCommentCount);
        this.imageContent = v.findViewById(R.id.imageContent);
        this.btnLike = v.findViewById(R.id.btnLike);
        this.btnComment = v.findViewById(R.id.btnComment);
        this.btnMoreOptions = v.findViewById(R.id.btnMoreOptions);
        this.imageContentCardView = v.findViewById(R.id.imageContentCardView);
    }
}
