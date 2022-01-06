package com.example.assignment3.chat;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment3.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    AppCompatTextView messageTextView, messageGroupDate;
    AppCompatImageView messageImage;
    AppCompatTextView messageTimestamp;
    CircleImageView messengerIcon;
    public MessageViewHolder( View itemView) {
        super(itemView);
        this.messageTextView = itemView.findViewById(R.id.chat_messageTextView);
        this.messageImage = itemView.findViewById(R.id.chat_messageImage);
        this.messengerIcon = itemView.findViewById(R.id.chat_messengerIcon);
        this.messageTimestamp = itemView.findViewById(R.id.chat_messageTimestamp);
        this.messageGroupDate = itemView.findViewById(R.id.chat_groupDate);
    }
}
