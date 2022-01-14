package com.example.assignment3.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.assignment3.IChatManagement;
import com.example.assignment3.R;
import com.example.assignment3.chat.ChatRoomAdapter;
import com.example.assignment3.chat.ChatRoomViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.concurrent.Executor;

public class ChatRoom extends Fragment {
    private final String TAG = "ChatRoomActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirestoreRecyclerAdapter<com.example.assignment3.models.ChatRoom, ChatRoomViewHolder>
            chatRoomAdapter;

    private String currentUserId;
    private FirebaseFirestore firebaseFirestore;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView rvChatRoom;
    private NestedScrollView layoutRecyclerView;
    private RelativeLayout layoutRoomChatNoMessage;
    private IChatManagement listener;
    private ListenerRegistration registration;
    private AppCompatImageButton btnBack;

    public ChatRoom() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");

        /*
         *   Represents a Cloud Firestore database and
         *   is the entry point for all Cloud Firestore
         *   operations.
         */
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        currentUserId = currentUser.getUid();

        //get listener
        listener = (IChatManagement) getActivity();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);
        setupViews(view);
        fillChatRoom();
        return view;
    }

    private void setupViews(View view) {
        layoutRecyclerView = view.findViewById(R.id.layoutRecyclerView);
        layoutRoomChatNoMessage = view.findViewById(R.id.layoutRoomChatNoMessage);
        btnBack = view.findViewById(R.id.btnBack);
        rvChatRoom = view.findViewById(R.id.recyclerViewChatRoom);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(true);
        rvChatRoom.setLayoutManager(mLinearLayoutManager);

        //set listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

    }

    private void fillChatRoom() {
        /**
         * Query all chat rooms
         * */
        Query query = firebaseFirestore
                .collection("chats")
                .whereArrayContains("userIDs", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING);


        /**
         * Configure recycler adapter options:
         *  * query is the Query object defined above.
         *  * ChatRoom.class instructs the adapter to convert each DocumentSnapshot to a ChatRoom object
         * */
        FirestoreRecyclerOptions<com.example.assignment3.models.ChatRoom> options =
                new FirestoreRecyclerOptions.Builder<com.example.assignment3.models.ChatRoom>()
                        .setQuery(query, com.example.assignment3.models.ChatRoom.class)
                        .setLifecycleOwner(this)
                        .build();

        /**
         * Create the FirestoreRecyclerAdapter object
         * */

        chatRoomAdapter = new ChatRoomAdapter(options, getContext(), getParentFragmentManager(), listener);

        //set adapter and layout manager for RecyclerView
        rvChatRoom.setAdapter(chatRoomAdapter);
        /**
         * Add SnapshotListener to get chat rooms in realtime
         * */
        registration = query.addSnapshotListener( (snapshot, error) -> {
            if (error != null) {
                //Handle error
                return;
            }
            //Convert query snapshot to a list of message
            List<com.example.assignment3.models.ChatRoom>
                    chatRooms = snapshot.toObjects(com.example.assignment3.models.ChatRoom.class);
            Log.i(TAG, chatRooms.toString());
            //Update UI
            if (chatRooms.isEmpty()) {
                layoutRecyclerView.setVisibility(View.GONE);
                layoutRoomChatNoMessage.setVisibility(View.VISIBLE);
            } else {
                layoutRecyclerView.setVisibility(View.VISIBLE);
                layoutRoomChatNoMessage.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registration.remove();
    }
}