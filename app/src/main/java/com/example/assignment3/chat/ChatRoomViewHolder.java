package com.example.assignment3.chat;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment3.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomViewHolder extends RecyclerView.ViewHolder {
    CircleImageView imageChatRoomAdapter;
    AppCompatTextView usernameChatRoomAdapter, lastMessageChatRoomAdapter, timestamp;
    public ChatRoomViewHolder(@NonNull View itemView) {
        super(itemView);
        this.imageChatRoomAdapter = itemView.findViewById(R.id.imageChatRoomAdapter);
        this.usernameChatRoomAdapter = itemView.findViewById(R.id.usernameChatRoomAdapter);
        this.lastMessageChatRoomAdapter = itemView.findViewById(R.id.lastMessageChatRoomAdapter);
        this.timestamp = itemView.findViewById(R.id.timestamp);
    }
}
