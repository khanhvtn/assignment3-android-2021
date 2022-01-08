package com.example.assignment3.notificationCenter;


import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment3.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationCenterViewHolder extends RecyclerView.ViewHolder {
    LinearLayoutCompat notificationWrapper;
    AppCompatTextView groupDate, messageNotification, messageTimestamp;
    CircleImageView messageAvatar;


    public NotificationCenterViewHolder(View v) {
        super(v);
        this.notificationWrapper = v.findViewById(R.id.notificationWrapper);
        this.groupDate = v.findViewById(R.id.groupDate);
        this.messageAvatar = v.findViewById(R.id.messageAvatar);
        this.messageNotification = v.findViewById(R.id.messageNotification);
        this.messageTimestamp = v.findViewById(R.id.messageTimestamp);
    }
}
