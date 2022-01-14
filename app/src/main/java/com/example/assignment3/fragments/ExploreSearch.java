package com.example.assignment3.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.assignment3.Explore.SearchUserAdapter;
import com.example.assignment3.IExploreManagement;
import com.example.assignment3.R;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ExploreSearch extends Fragment {
    private final String TAG = "ExploreSearch";
    private RecyclerView rv_user;
    private SearchUserAdapter searchUserAdapter;
    private IExploreManagement exploreManagement;
    private LinearLayoutManager mLinearLayoutManager;

    public ExploreSearch() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exploreManagement = (IExploreManagement) getParentFragment();

        mLinearLayoutManager = new LinearLayoutManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_explore_search, container, false);
        rv_user = v.findViewById(R.id.rv_user);
        exploreManagement.setRvUser(rv_user);
        rv_user.setLayoutManager(mLinearLayoutManager);

        //get all users
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .whereNotEqualTo("userId", Utility.firebaseAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<User> listUsers = queryDocumentSnapshots.toObjects(User.class);
                                searchUserAdapter =
                                        new SearchUserAdapter(listUsers, rv_user, getContext());
                                exploreManagement.setSearchUserAdapter(searchUserAdapter);
                                rv_user.setAdapter(searchUserAdapter);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, e.getMessage());
                Utility.ToastMessage("Something went wrong!", getContext());
            }
        });
        return v;
    }
}