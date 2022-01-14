package com.example.assignment3.notificationCenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.example.assignment3.CommentActivity;
import com.example.assignment3.R;
import com.example.assignment3.UserProfileActivity;
import com.example.assignment3.models.Comment;
import com.example.assignment3.models.Like;
import com.example.assignment3.models.NotificationApp;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.HashMap;

public class NotificationCenterAdapter
        extends FirestoreRecyclerAdapter<NotificationApp, NotificationCenterViewHolder> {
    private HashMap<String, Date> groupDates = new HashMap<>();
    private HashMap<String, AppCompatTextView> groupTextViews = new HashMap<>();
    private static final String TAG = "NotificationAdapter";
    private Context context;


    public NotificationCenterAdapter(FirestoreRecyclerOptions<NotificationApp> options,
                                     Context context) {
        super(options);
        this.context = context;

    }


    @Override
    protected void onBindViewHolder(@NonNull
                                            NotificationCenterViewHolder viewHolder, int position,
                                    @NonNull NotificationApp notification) {

        //add avatar
        Utility.firebaseFirestore.collection(context.getString(R.string.user_collection))
                .document(notification.getUserId()).get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user.getImageFileName() != null) {
                            Utility.firebaseStorage.getReference()
                                    .child("images/" + user.getImageFileName()).getDownloadUrl()
                                    .addOnSuccessListener(
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    Glide.with(context).load(uri)
                                                            .into(viewHolder.messageAvatar);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, e.getMessage());
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, e.getMessage());
            }
        });


        viewHolder.messageNotification.setText(notification.getMessage());

        if (notification.getTimestamp() != null) {
            //set notification timestamp
            viewHolder.messageTimestamp.setText(
                    Utility.calculateDifferentWithCurrentTime(notification.getTimestamp()));

            //set group date if groupDate null or changed.
            DateTimeFormatter dtf2 = DateTimeFormat.forPattern("MMM dd, yyyy");
            String newGroupDate = dtf2.print(notification.getTimestamp().getTime());
            String currentDate = dtf2.print(new DateTime().toDate().getTime());

            if (groupDates.containsKey(newGroupDate)) {
                Date oldestDate = groupDates.get(newGroupDate);
                if (oldestDate.compareTo(notification.getTimestamp()) == -1) {
                    viewHolder.groupDate.setVisibility(View.VISIBLE);
                    //update group date
                    groupDates.put(newGroupDate, notification.getTimestamp());
                    //update group text view
                    groupTextViews.get(newGroupDate).setVisibility(View.GONE);
                    groupTextViews.put(newGroupDate, viewHolder.groupDate);

                }
            } else {
                viewHolder.groupDate.setVisibility(View.VISIBLE);
                groupDates.put(newGroupDate, notification.getTimestamp());
                groupTextViews.put(newGroupDate, viewHolder.groupDate);
            }


            if (newGroupDate.equals(currentDate)) {
                viewHolder.groupDate.setText("Today");
            } else {
                viewHolder.groupDate.setText(newGroupDate);
            }
        }

        //set listener for each item
        viewHolder.notificationWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (notification.getType().equals("post")) {
                    //check post is exists or not
                    Utility.firebaseFirestore
                            .collection(context.getString(R.string.post_collection))
                            .document(notification.getTargetId()).get().addOnSuccessListener(
                            new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Intent intent;
                                    if (documentSnapshot.getData() != null) {
                                        intent = new Intent(context, CommentActivity.class);
                                        intent.putExtra("postId", notification.getTargetId());
                                        context.startActivity(intent);
                                    } else {
                                        Utility.ToastMessage("This post no longer exists", context);
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                } else {
                    intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("userId", notification.getTargetId());
                    context.startActivity(intent);
                }

            }
        });

    }


    @NonNull
    @Override
    public NotificationCenterViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.item_notification, group, false);
        return new NotificationCenterViewHolder(view);
    }


    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
