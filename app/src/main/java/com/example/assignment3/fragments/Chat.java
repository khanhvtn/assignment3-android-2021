package com.example.assignment3.fragments;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.assignment3.BuildConfig;
import com.example.assignment3.IChatManagement;
import com.example.assignment3.R;
import com.example.assignment3.chat.MessageViewHolder;
import com.example.assignment3.models.ChatRoom;
import com.example.assignment3.models.Message;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class Chat extends Fragment {
    private static final String CAPTURE_IMAGE_DIALOG_TAG = "CAPTURE_IMAGE_DIALOG_TAG";
    private static final String TAG = "ChatFragment";
    private ImageButton btnImageAddPhoto, btnImageSendMessage, btnBack, btnLikeMessage;
    private RecyclerView rvChat;
    private EditText edtChat;
    private TextView txtNameReceiver;
    private FirestoreRecyclerAdapter<Message, MessageViewHolder>
            chatAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser currentUser;
    private String authorID, authorName, authorImageFile;
    private String currentRoomChatID;
    private ActivityResultLauncher captureImageLauncher, pickImageFromPhoto;
    private CaptureImageDialogFragment captureImageDialogFragment;
    private Uri imageUri;
    private FirebaseStorage storage;
    private IChatManagement listener;
    private User currentUserInfo;
    private CircleImageView imageReceiver;
    private Boolean startFromProfile = false;

    public Chat() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //get listener
        listener = (IChatManagement) getActivity();

        //create temporary image
        File newFile = new File(getContext().getFilesDir(), "default_image.jpg");
        imageUri = FileProvider
                .getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", newFile);
        //set activity result launcher
        captureImageLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        Log.i(TAG, "Capture Image");
                        if (result) {
                            Log.i(TAG, "Capture Image Successfully");
                            String nameImage = UUID.randomUUID().toString() + ".jpg";
                            UploadTask uploadTask =
                                    storage.getReference().child("images/" + nameImage)
                                            .putFile(imageUri);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    ToastMessage(
                                            "Something went wrong with uploading image. Please try again!!!");
                                }
                            });
                            uploadTask.addOnSuccessListener(
                                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(
                                                UploadTask.TaskSnapshot taskSnapshot) {
                                            sendMessage(null, null, nameImage);
                                        }
                                    });
                        }
                    }
                });
        pickImageFromPhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            imageUri = result;
                            Log.i(TAG, "Pick Image Successfully");
                            String nameImage = UUID.randomUUID().toString() + ".jpg";
                            UploadTask uploadTask =
                                    storage.getReference().child("images/" + nameImage)
                                            .putFile(imageUri);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    ToastMessage(
                                            "Something went wrong with uploading image. Please try again!!!");
                                }
                            });
                            uploadTask.addOnSuccessListener(
                                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(
                                                UploadTask.TaskSnapshot taskSnapshot) {
                                            sendMessage(null, null, nameImage);
                                        }
                                    });
                        }
                    }
                });

        listener.setUpForPickImageDialog(captureImageLauncher, pickImageFromPhoto, imageUri);

        //get authorInfo from intent
        //[authorID, authorName]
        getParentFragmentManager()
                .setFragmentResultListener("requestKey", this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey,
                                                 @NonNull Bundle bundle) {
                        String[] authorInfo = bundle.getStringArray("author_info");
                        Boolean isStartFromProfile = bundle.getBoolean("startFromProfile");
                        if (isStartFromProfile != null) {
                            startFromProfile = isStartFromProfile;
                        }

                        authorID = authorInfo[0];
                        authorName = authorInfo[1];
                        authorImageFile = authorInfo[2];

                        //set author name in toolbar
                        txtNameReceiver.setText(authorName);

                        //set author image if it exists
                        if (!authorImageFile.isEmpty()) {
                            storage.getReference()
                                    .child("images/" + authorImageFile)
                                    .getDownloadUrl().addOnSuccessListener(
                                    new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //set image receiver image
                                            Glide.with(imageReceiver
                                                    .getContext())
                                                    .load(uri)
                                                    .into(imageReceiver);
                                            /**
                                             * Get ID Room Chat
                                             * If this is a first conversation of both users, the app will create a new room chat
                                             * and return new id room chat.
                                             * */
                                            //gen RoomID
                                            String roomID =
                                                    Utility.genUID(authorID, currentUser.getUid());
                                            firebaseFirestore.collection("chats")
                                                    .whereEqualTo("roomID", roomID)
                                                    .get()
                                                    .addOnCompleteListener(
                                                            new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull
                                                                                               Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        if (task.getResult()
                                                                                .isEmpty()) {
                                                                            com.example.assignment3.models.ChatRoom
                                                                                    chatRoom =
                                                                                    new ChatRoom(
                                                                                            roomID,
                                                                                            Arrays.asList(
                                                                                                    currentUser
                                                                                                            .getUid(),
                                                                                                    authorID));
                                                                            firebaseFirestore
                                                                                    .collection(
                                                                                            "chats")
                                                                                    .document(
                                                                                            roomID)
                                                                                    .set(chatRoom)
                                                                                    .addOnCompleteListener(
                                                                                            new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(
                                                                                                        @NonNull
                                                                                                                Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        Log.v(TAG,
                                                                                                                "New room chat ID: " +
                                                                                                                        roomID);
                                                                                                        currentRoomChatID =
                                                                                                                roomID;
                                                                                                        openRoomChat(
                                                                                                                uri);
                                                                                                    }
                                                                                                }
                                                                                            });

                                                                        } else {
                                                                            Log.v(TAG,
                                                                                    "Room chat ID: " +
                                                                                            task.getResult()
                                                                                                    .size());
                                                                            /**
                                                                             * The app surely that the query only return 1 document.
                                                                             * Therefore, the app will get the first document.
                                                                             * */
                                                                            String roomChatID =
                                                                                    task.getResult()
                                                                                            .getDocuments()
                                                                                            .get(0)
                                                                                            .getId();
                                                                            currentRoomChatID =
                                                                                    roomChatID;
                                                                            openRoomChat(uri);
                                                                        }
                                                                    }
                                                                }
                                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    ToastMessage(e.getMessage());
                                    Log.w(TAG, "Getting download url was not successful.");
                                }
                            });
                        } else {
                            /**
                             * Get ID Room Chat
                             * If this is a first conversation of both users, the app will create a new room chat
                             * and return new id room chat.
                             * */
                            //gen RoomID
                            String roomID = Utility.genUID(authorID, currentUser.getUid());
                            firebaseFirestore.collection("chats")
                                    .whereEqualTo("roomID", roomID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (task.getResult().isEmpty()) {
                                                    com.example.assignment3.models.ChatRoom
                                                            chatRoom =
                                                            new ChatRoom(roomID,
                                                                    Arrays.asList(
                                                                            currentUser.getUid(),
                                                                            authorID));
                                                    firebaseFirestore.collection("chats")
                                                            .document(roomID)
                                                            .set(chatRoom)
                                                            .addOnCompleteListener(
                                                                    new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(
                                                                                @NonNull
                                                                                        Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Log.v(TAG,
                                                                                        "New room chat ID: " +
                                                                                                roomID);
                                                                                currentRoomChatID =
                                                                                        roomID;
                                                                                openRoomChat(null);
                                                                            }
                                                                        }
                                                                    });

                                                } else {
                                                    Log.v(TAG,
                                                            "Room chat ID: " +
                                                                    task.getResult().size());
                                                    /**
                                                     * The app surely that the query only return 1 document.
                                                     * Therefore, the app will get the first document.
                                                     * */
                                                    String roomChatID =
                                                            task.getResult().getDocuments().get(0)
                                                                    .getId();
                                                    currentRoomChatID = roomChatID;
                                                    openRoomChat(null);
                                                }
                                            }
                                        }
                                    });
                        }


                    }
                });

        //get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //create firebase instance service
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        //get current user info
        firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user = documentSnapshot.toObject(User.class);
                                currentUserInfo = user;
                            }
                        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        //create instance for views
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(true);
        btnImageAddPhoto = view.findViewById(R.id.chat_btnImageAddPhoto);
        btnImageSendMessage = view.findViewById(R.id.chat_btnImageSendMessage);
        btnLikeMessage = view.findViewById(R.id.chat_btnLikeMessage);
        btnBack = view.findViewById(R.id.chat_btnBack);
        rvChat = view.findViewById(R.id.chat_rvChat);
        edtChat = view.findViewById(R.id.chat_edtChat);
        txtNameReceiver = view.findViewById(R.id.chat_nameReceiver);
        imageReceiver = view.findViewById(R.id.chat_imageReceiver);

        rvChat.setLayoutManager(mLinearLayoutManager);


        // Set on click listener
        onClickListener();
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClickListener() {
        //set listener for views
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startFromProfile) {
                    getActivity().finish();
                } else {
                    listener.switchFragmentInMainActivity(
                            new com.example.assignment3.fragments.ChatRoom());
                }

            }
        });

        // Add photo to message
        btnImageAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("chat", "Add Photo");
                Log.v("chat", getChildFragmentManager().toString());
                captureImageDialogFragment = new CaptureImageDialogFragment();

                captureImageDialogFragment
                        .show(getChildFragmentManager(), CAPTURE_IMAGE_DIALOG_TAG);
            }
        });

        // Image send message
        btnImageSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("chat", "Send Message");
                sendMessage(Objects.requireNonNull(edtChat.getText()).toString(), null, null);
                updateAuthorSeenStatus();
            }
        });
        btnLikeMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("chat", "Send Message");
                char[] icon = Character.toChars(0x1F44D);
                StringBuilder stringBuilder = new StringBuilder();
                for (char ch : icon
                ) {
                    stringBuilder.append(ch);
                }
                sendMessage(stringBuilder.toString(), null, null);
                updateAuthorSeenStatus();
            }
        });

        /**
         * If user do not input any message. Then the app will disable sent message button
         * */
        edtChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    btnImageSendMessage.setVisibility(View.VISIBLE);
                    btnLikeMessage.setVisibility(View.GONE);
                } else {
                    btnImageSendMessage.setVisibility(View.GONE);
                    btnLikeMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                firebaseFirestore.collection("chats")
                        .document(currentRoomChatID).get().addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.getData() != null) {
                                    firebaseFirestore.collection("chats")
                                            .document(currentRoomChatID)
                                            .update(currentUser.getUid(), true)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Set current user seen!");
                                                    }
                                                }
                                            });

                                    firebaseFirestore.collection("chats")
                                            .document(currentRoomChatID)
                                            .collection("messages")
                                            .whereEqualTo("seenStatus", false)
                                            .whereEqualTo("uidSender", authorID)
                                            .get()
                                            .addOnCompleteListener(
                                                    new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(
                                                                @NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                Map<String, Object> seenStatus =
                                                                        new HashMap<>();
                                                                seenStatus.put("seenStatus", true);
                                                                if (task.getResult() != null) {
                                                                    for (QueryDocumentSnapshot message : task
                                                                            .getResult()) {
                                                                        message.getReference()
                                                                                .set(seenStatus,
                                                                                        SetOptions
                                                                                                .merge());
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });
                                }
                            }
                        });
                return false;
            }
        });
    }

    private void updateAuthorSeenStatus() {
        firebaseFirestore.collection("chats")
                .document(currentRoomChatID)
                .update(authorID, false)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Set author not seen!");
                        }
                    }
                });
    }

    private void openRoomChat(Uri uri) {

        /**
         * Query 50 most recent chat messages
         * */
        Query query = firebaseFirestore
                .collection("chats")
                .document(currentRoomChatID)
                .collection("messages")
                .orderBy("timestamp")
                .limitToLast(100);

        /**
         * Add SnapshotListener to get message in realtime
         * */
        query.addSnapshotListener(getActivity(), (snapshot, error) -> {
            if (error != null) {
                //Handle error
                return;
            }
            //Convert query snapshot to a list of message
            List<Message> messages = snapshot.toObjects(Message.class);

            //Update UI
        });

        /**
         * Configure recycler adapter options:
         *  * query is the Query object defined above.
         *  * Message.class instructs the adapter to convert each DocumentSnapshot to a Message object
         * */
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .setLifecycleOwner(this)
                .build();

        /**
         * Create the FirestoreRecyclerAdapter object
         * */

        chatAdapter = new com.example.assignment3.chat.ChatAdapter(options, getContext(), uri);

        //scroll to newest message
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = chatAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    Log.v(TAG, "Item scroll to bottom: " + lastVisiblePosition);
                    rvChat.scrollToPosition(positionStart);
                }
            }
        });

        //set adapter and layout manager for RecyclerView
        rvChat.setAdapter(chatAdapter);


    }

    private void sendMessage(String message, String photoUrl, String imageUrl) {
        Message newMessage = new
                Message(currentUser.getUid(),
                currentUserInfo.getFullName(),
                message,
                photoUrl,
                imageUrl,
                false);

        firebaseFirestore.collection("chats").document(currentRoomChatID)
                .collection("messages").add(newMessage)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "New Chat added with ID: " + documentReference.getId());
                        //update timestamp roomchat
                        Map<String, Object> timestamp = new HashMap<>();
                        timestamp.put("timestamp", FieldValue.serverTimestamp());
                        firebaseFirestore.collection("chats")
                                .document(currentRoomChatID).set(timestamp, SetOptions.merge());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }
        });
        edtChat.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}