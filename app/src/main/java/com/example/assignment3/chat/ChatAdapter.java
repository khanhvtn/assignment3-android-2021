package com.example.assignment3.chat;

import android.content.Context;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.assignment3.R;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatAdapter extends FirestoreRecyclerAdapter<Message, MessageViewHolder> {
    private HashMap<String, Date> groupDates = new HashMap<>();
    private HashMap<String, AppCompatTextView> groupTextViews = new HashMap<>();
    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;
    private static final String TAG = "ChatAdapter";
    private Context context;
    private FirebaseUser currentUser;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Uri imageReceiverUri;


    public ChatAdapter(FirestoreRecyclerOptions<Message> options, Context context,
                       Uri imageReceiverUri) {
        super(options);
        this.context = context;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.imageReceiverUri = imageReceiverUri;

    }


    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int position,
                                    @NonNull Message message) {


        if (getItemViewType(position) == MSG_LEFT) {
            /**
             * Set Avatar for User
             * */
            Log.i(TAG, "Set Icon for Message Left");
            if (imageReceiverUri != null) {
                Glide.with(context)
                        .load(imageReceiverUri)
                        .into(messageViewHolder.messengerIcon);
            }

        }


        /**
         * Set message information for MessageViewHolder
         * if text message exists, the app will display text message and hide image message and vice versa.
         * */
        if (message.getMessage() != null) {
            messageViewHolder.messageTextView.setText(message.getMessage());
            messageViewHolder.messageTextView.setVisibility(TextView.VISIBLE);
            messageViewHolder.messageImage.setVisibility(ImageView.GONE);
        } else if (message.getImageUrl() != null) {
            String imageUrl = message.getImageUrl();
            storage.getReference().child("images/" + imageUrl)
                    .getDownloadUrl().addOnSuccessListener(
                    new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context).load(uri).into(messageViewHolder.messageImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastMessage(e.getMessage());
                    Log.w(TAG, "Getting download url was not successful.");
                }
            });

            messageViewHolder.messageImage.setVisibility(ImageView.VISIBLE);
            messageViewHolder.messageTextView.setVisibility(TextView.GONE);
        }

        if (message.getTimestamp() != null) {
            //set message timestamp
            //h:mm a
            DateTimeFormatter dtf = DateTimeFormat.forPattern("h:mm a");
            messageViewHolder.messageTimestamp.setText(dtf.print(message.getTimestamp().getTime()));


            //set group date if groupDate null or changed.
            DateTimeFormatter dtf2 = DateTimeFormat.forPattern("MMM dd, yyyy");
            String newGroupDate = dtf2.print(message.getTimestamp().getTime());
            String currentDate = dtf2.print(new DateTime().toDate().getTime());

            if (groupDates.containsKey(newGroupDate)) {
                Date oldestDate = groupDates.get(newGroupDate);
                if (oldestDate.compareTo(message.getTimestamp()) == 1) {
                    messageViewHolder.messageGroupDate.setVisibility(View.VISIBLE);
                    //update group date
                    groupDates.put(newGroupDate, message.getTimestamp());
                    //update group text view
                    groupTextViews.get(newGroupDate).setVisibility(View.GONE);
                    groupTextViews.put(newGroupDate, messageViewHolder.messageGroupDate);

                }
            } else {
                messageViewHolder.messageGroupDate.setVisibility(View.VISIBLE);
                groupDates.put(newGroupDate, message.getTimestamp());
                groupTextViews.put(newGroupDate, messageViewHolder.messageGroupDate);
            }


            if (newGroupDate.equals(currentDate)) {
                messageViewHolder.messageGroupDate.setText("Today");
            } else {
                messageViewHolder.messageGroupDate.setText(newGroupDate);
            }
        }

    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        /**
         * Create a new instance of the ViewHolder.
         * If current user is sender, then the app display item_message_left layout
         * If current user is not sender, then the app display item_message_right layout
         * */
        // Create a new instance of the ViewHolder, in this case we are using a custom
        // layout called R.layout.message for each item
        View view;
        if (viewType == MSG_RIGHT) {
            view = LayoutInflater.from(group.getContext())
                    .inflate(R.layout.item_message_right, group, false);
        } else {
            view = LayoutInflater.from(group.getContext())
                    .inflate(R.layout.item_message_left, group, false);
        }
        return new MessageViewHolder(view);
    }


    @Override
    public int getItemViewType(int position) {
        Log.v(TAG, "Position: " + position);

        if (getItem(position).getUidSender().equals(currentUser.getUid())) {
            return MSG_RIGHT;
        } else {
            return MSG_LEFT;
        }

    }

    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
