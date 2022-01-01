package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.example.assignment3.notification.Notification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Integer idNotification = 1;
    private final String CHANNEL_ID = UUID.randomUUID().toString();
    private final String TAG = "MainActivity";
    private android.app.Notification userNotification;
    private NotificationCompat.Builder builder;
    private ListenerRegistration
            listenerRegistrationNotification;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //create notification
        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannel();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, Authentication.class));
        }

        AppCompatButton btnLogout = findViewById(R.id.main_btnLogout);
        AppCompatButton btnChatRoom = findViewById(R.id.main_btnChatRoom);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(view.getContext(), Authentication.class));
            }
        });
        btnChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), ChatActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            listenerRegistrationNotification =
                    db.collection("users").document(mAuth.getCurrentUser().getUid())
                            .collection(getResources().getString(R.string.notifications_collection))
                            .addSnapshotListener(
                                    new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value,
                                                            @Nullable
                                                                    FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "listen:error", e);
                                                return;
                                            }

                                            for (DocumentChange dc : value.getDocumentChanges()) {
                                                switch (dc.getType()) {
                                                    case ADDED:
                                                        Notification
                                                                newNotification =
                                                                dc.getDocument().toObject(
                                                                        Notification.class);
                                                        builder =
                                                                new NotificationCompat.Builder(MainActivity.this,
                                                                        CHANNEL_ID)
                                                                        .setSmallIcon(
                                                                                R.drawable.ic_person)
                                                                        .setContentTitle("New Post")
                                                                        .setContentText(
                                                                                newNotification.getMessage())
                                                                        .setStyle(
                                                                                new NotificationCompat.BigTextStyle()
                                                                                        .bigText(
                                                                                                newNotification.getMessage()))
                                                                        .setPriority(
                                                                                NotificationCompat.PRIORITY_DEFAULT);
                                                        userNotification = builder.build();
                                                        notificationManager
                                                                .notify(idNotification++,
                                                                        userNotification);
                                                        dc.getDocument().getReference().delete()
                                                                .addOnFailureListener(
                                                                        new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(
                                                                                    @NonNull
                                                                                            Exception e) {
                                                                                ToastMessage(
                                                                                        e.getMessage());
                                                                            }
                                                                        });
                                                        break;
                                                    case MODIFIED:
                                                        Log.d(TAG,
                                                                "Modified city: " +
                                                                        dc.getDocument().getData());
                                                        break;
                                                    case REMOVED:
                                                        Log.d(TAG,
                                                                "Removed city: " +
                                                                        dc.getDocument().getData());
                                                        break;
                                                }
                                            }
                                        }
                                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listenerRegistrationNotification.remove();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_user_notification);
            String description = getString(R.string.channel_user_notification_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}