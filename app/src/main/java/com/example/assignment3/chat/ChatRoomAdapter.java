package com.example.assignment3.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.assignment3.IChatManagement;
import com.example.assignment3.R;
import com.example.assignment3.fragments.Chat;
import com.example.assignment3.models.ChatRoom;
import com.example.assignment3.models.Message;
import com.example.assignment3.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatRoomAdapter extends FirestoreRecyclerAdapter<ChatRoom, ChatRoomViewHolder> {
    private static final String TAG = "ChatRoomAdapter";
    private final Context context;
    private final FragmentManager parentFragmentManager;
    private final IChatManagement listener;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private ListenerRegistration registration;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     * @param parentFragmentManager
     * @param listener
     */
    public ChatRoomAdapter(@NonNull FirestoreRecyclerOptions<ChatRoom> options, Context context,
                           FragmentManager parentFragmentManager,
                           IChatManagement listener) {
        super(options);
        this.context = context;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.parentFragmentManager = parentFragmentManager;
        this.listener = listener;
    }


    @Override
    protected void onBindViewHolder(@NonNull ChatRoomViewHolder chatRoomViewHolder, int position,
                                    @NonNull ChatRoom chatRoom) {

        String receiverID = getIdReceiver(chatRoom);
        firebaseFirestore.collection("users")
                .document(receiverID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User receiverInfo = task.getResult().toObject(User.class);

                            /**
                             * Set avatar icon and name for receiver.
                             * **/
                            chatRoomViewHolder.usernameChatRoomAdapter
                                    .setText(receiverInfo.getFullName());


                            if (receiverInfo.getImageFileName() != null) {
                                firebaseStorage.getReference()
                                        .child("images/" + receiverInfo.getImageFileName())
                                        .getDownloadUrl().addOnSuccessListener(
                                        new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Glide.with(chatRoomViewHolder.imageChatRoomAdapter
                                                        .getContext())
                                                        .load(uri)
                                                        .into(chatRoomViewHolder.imageChatRoomAdapter);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        ToastMessage(e.getMessage());
                                        Log.w(TAG, "Getting download url was not successful.");
                                    }
                                });

                            } else {
                                chatRoomViewHolder.imageChatRoomAdapter
                                        .setImageResource(R.drawable.ic_person);
                            }

                            //set onclick listener

                            chatRoomViewHolder.itemView
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Bundle result = new Bundle();
                                            String[] authorInfo =
                                                    {receiverID, receiverInfo.getFullName(),
                                                            receiverInfo.getImageFileName() !=
                                                                    null ? receiverInfo
                                                                    .getImageFileName() : ""};

                                            result.putStringArray("author_info", authorInfo);
                                            parentFragmentManager
                                                    .setFragmentResult("requestKey", result);
                                            registration.remove();
                                            listener.switchFragmentInMainActivity(new Chat());
                                        }
                                    });
                        }
                    }
                });

        Query query = firebaseFirestore.collection("chats")
                .document(chatRoom.getRoomID())
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1);

        registration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle error
                    //...
                    return;
                }

                // Convert query snapshot to a list of chats
                List<Message> messages = snapshot.toObjects(Message.class);


                // Update UI
                // ...

                /**
                 * 1. No message will display the last message is "Say Hi! To Chat".
                 * 2. The last message is a photo, then display the last message is "Sent a photo".
                 * 3. The last message is a text, then display the text.
                 * */
                if (messages.isEmpty()) {
                    chatRoomViewHolder.lastMessageChatRoomAdapter.setText("Say Hi! To Chat");
                    chatRoomViewHolder.lastMessageChatRoomAdapter.setTypeface(null, Typeface.BOLD);
                } else {
                    Message message = messages.get(0);
                    Boolean seenStatus = message.getSeenStatus();
                    String displayFinalMessage = "";
                    if (message.getTimestamp() != null) {
                        //set timestamp for chatroom
                        Period p = new Period(message.getTimestamp().getTime(),
                                Instant.now().toDate().getTime());
                        if (p.getDays() > 0) {
                            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
                            chatRoomViewHolder.timestamp
                                    .setText(dtf.print(message.getTimestamp().getTime()));
                        } else if (p.getHours() > 0) {
                            chatRoomViewHolder.timestamp.setText(p.getHours() + "h ago");
                        } else if (p.getMinutes() > 0) {
                            chatRoomViewHolder.timestamp.setText(p.getMinutes() + "m ago");
                        } else {
                            chatRoomViewHolder.timestamp.setText(
                                    p.getSeconds() == 0 ? "1s ago" : p.getSeconds() + "s ago");
                        }
                    }


                    if (message.getMessage() == null) {
                        displayFinalMessage = "Sent a photo";
                    } else {
                        displayFinalMessage = message.getMessage();
                    }

                    /**
                     * If the message is unseen. The message will display with bold style.
                     * * However, if the sender message is current user, the message will display
                     * * with normal style.
                     * * If the sender message  is not current user, the message will display with
                     * * bold style.
                     * If the message is seen. The message will display with normal style.
                     * */
                    if (seenStatus) {
                        chatRoomViewHolder.lastMessageChatRoomAdapter.setText(displayFinalMessage);
                        chatRoomViewHolder.lastMessageChatRoomAdapter
                                .setTypeface(null, Typeface.NORMAL);
                    } else {
                        if (message.getUidSender().equals(currentUser.getUid())) {
                            chatRoomViewHolder.lastMessageChatRoomAdapter
                                    .setText(displayFinalMessage);
                            chatRoomViewHolder.lastMessageChatRoomAdapter
                                    .setTypeface(null, Typeface.NORMAL);
                        } else {
                            chatRoomViewHolder.lastMessageChatRoomAdapter
                                    .setText(displayFinalMessage);
                            chatRoomViewHolder.lastMessageChatRoomAdapter
                                    .setTypeface(null, Typeface.BOLD);
                        }
                    }
                }

            }
        });
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.item_chat_room, group, false);
        return new ChatRoomViewHolder(view);
    }

    private String getIdReceiver(ChatRoom chatRoom) {
        List<String> userIDs = chatRoom.getUserIDs();
        String receiverID = "";
        for (String userID : userIDs) {
            if (!userID.equals(currentUser.getUid())) {
                receiverID = userID;
                break;
            }
        }
        return receiverID;
    }

    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
