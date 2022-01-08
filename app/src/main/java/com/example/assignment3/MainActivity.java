package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.assignment3.fragments.CurrentUserProfile;
import com.example.assignment3.fragments.Explore;
import com.example.assignment3.fragments.Home;
import com.example.assignment3.fragments.NotificationCenter;
import com.example.assignment3.models.NotificationApp;
import com.example.assignment3.utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements IMainManagement {
    private Integer idNotification = 1;
    private final String CHANNEL_ID = UUID.randomUUID().toString();
    private final String TAG = "MainActivity";
    private BottomNavigationView main_bottomNavigationBar;
    private NotificationCompat.Builder builder;
    private View itemProfile;
    private NotificationManagerCompat notificationManager;
    private ListenerRegistration
            listenerRegistrationNotification;
    private AppCompatImageButton main_btnCreatePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create notification
        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannel();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, Authentication.class));
        }

        //declare fields
        main_bottomNavigationBar = findViewById(R.id.main_bottomNavigationBar);
        main_btnCreatePost = findViewById(R.id.main_btnCreatePost);


        //add map fragment to activity
        Fragment homeFragment = new Home();
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_fragmentContainer, homeFragment,
                        homeFragment.getClass().toString())
                .commit();


        //declare fields
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
        itemProfile = main_bottomNavigationBar.findViewById(R.id.item_profile);

        main_bottomNavigationBar.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_home:
                                switchFragmentInMainActivity(new Home());
                                break;
                            case R.id.item_explore:
                                switchFragmentInMainActivity(new Explore());
                                break;
                            case R.id.item_notifications:
                                switchFragmentInMainActivity(new NotificationCenter());
                                break;
                            case R.id.item_profile:
                                switchFragmentInMainActivity(new CurrentUserProfile());
                                break;
                        }
                        return true;
                    }
                });

        main_btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreatePostActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if (Utility.firebaseAuth.getCurrentUser() != null) {
            listenerRegistrationNotification =
                    Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                            .document(Utility.firebaseAuth.getCurrentUser().getUid())
                            .collection(getResources().getString(R.string.notification_collection))
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value,
                                                    @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "listen:error", e);
                                        return;
                                    }

                                    for (DocumentChange dc : value.getDocumentChanges()) {
                                        switch (dc.getType()) {
                                            case ADDED:
                                                NotificationApp
                                                        newNotification =
                                                        dc.getDocument().toObject(
                                                                NotificationApp.class);
                                                builder =
                                                        new NotificationCompat.Builder(
                                                                MainActivity.this,
                                                                CHANNEL_ID)
                                                                .setSmallIcon(
                                                                        R.drawable.ic_person)
                                                                .setContentTitle(
                                                                        newNotification.getType()
                                                                                .equals("post") ?
                                                                                "New Post" :
                                                                                "New Follower")
                                                                .setContentText(
                                                                        newNotification
                                                                                .getMessage())
                                                                .setStyle(
                                                                        new NotificationCompat.BigTextStyle()
                                                                                .bigText(
                                                                                        newNotification
                                                                                                .getMessage()))
                                                                .setGroup(newNotification.getType())
                                                                .setPriority(
                                                                        NotificationCompat.PRIORITY_DEFAULT);
                                                notificationManager
                                                        .notify(idNotification++,
                                                                builder.build());
                                                //add to notification into notification permanent collection
                                                Utility.firebaseFirestore.collection(
                                                        getString(R.string.user_collection))
                                                        .document(Utility.firebaseAuth
                                                                .getCurrentUser().getUid())
                                                        .collection(getResources().getString(
                                                                R.string.notificationP_collection))
                                                        .add(new NotificationApp(
                                                                newNotification.getMessage(),
                                                                newNotification.getType(),
                                                                newNotification.getTargetId(),
                                                                newNotification.getUserId()));
                                                //delete new notification in general notification.
                                                dc.getDocument().getReference().delete()
                                                        .addOnFailureListener(
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(
                                                                            @NonNull Exception e) {
                                                                        Utility.ToastMessage(
                                                                                e.getMessage(),
                                                                                getApplicationContext());
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
    public void switchFragmentInMainActivity(Fragment fragment) {
        Fragment currentFragment =
                getSupportFragmentManager().findFragmentById(R.id.main_fragmentContainer);
        if (!fragment.getClass().toString().equals(currentFragment.getTag())) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    )
                    .setReorderingAllowed(true)
                    .replace(R.id.main_fragmentContainer, fragment, fragment.getClass().toString())
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (listenerRegistrationNotification != null) {
            listenerRegistrationNotification.remove();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (listenerRegistrationNotification != null) {
            listenerRegistrationNotification.remove();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void switchToProfile() {
        itemProfile.performClick();
    }

    @Override
    public void removeNotificationListener() {
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
}