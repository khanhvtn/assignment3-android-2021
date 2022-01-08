package com.example.assignment3.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.assignment3.IMainManagement;
import com.example.assignment3.R;
import com.example.assignment3.models.Message;
import com.example.assignment3.models.NotificationApp;
import com.example.assignment3.notificationCenter.NotificationCenterAdapter;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class NotificationCenter extends Fragment {
    private RecyclerView rv_notifications;
    private LinearLayoutManager mLinearLayoutManager;
    private NotificationCenterAdapter notificationCenterAdapter;
    private IMainManagement mainManagement;

    public NotificationCenter() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mainManagement = (IMainManagement) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_notification_center, container, false);
        rv_notifications = v.findViewById(R.id.rv_notifications);
        rv_notifications.setLayoutManager(mLinearLayoutManager);

        //create query
        Query query = Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(Utility.firebaseAuth.getCurrentUser().getUid())
                .collection(getString(R.string.notificationP_collection))
                .orderBy("timestamp").limitToLast(100);
        FirestoreRecyclerOptions<NotificationApp> options =
                new FirestoreRecyclerOptions.Builder<NotificationApp>()
                        .setQuery(query, NotificationApp.class)
                        .setLifecycleOwner(this)
                        .build();

        notificationCenterAdapter = new NotificationCenterAdapter(options, getContext());
        notificationCenterAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver(){
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                rv_notifications.scrollToPosition(0);
            }
        });
        rv_notifications.setAdapter(notificationCenterAdapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainManagement.switchFragmentInMainActivity(new NotificationCenter());
    }
}