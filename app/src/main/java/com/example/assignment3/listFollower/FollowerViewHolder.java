package com.example.assignment3.listFollower;


import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment3.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerViewHolder extends RecyclerView.ViewHolder {
    RelativeLayout layoutWrapper;
    CircleImageView imageUserAdapter;
    AppCompatTextView usernameUserAdapter;

    public FollowerViewHolder(View v) {
        super(v);
        this.layoutWrapper = v.findViewById(R.id.layoutWrapper);
        this.imageUserAdapter = v.findViewById(R.id.imageUserAdapter);
        this.usernameUserAdapter = v.findViewById(R.id.usernameUserAdapter);
    }
}
