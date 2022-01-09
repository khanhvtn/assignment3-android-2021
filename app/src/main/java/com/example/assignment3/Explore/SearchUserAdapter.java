package com.example.assignment3.Explore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment3.R;
import com.example.assignment3.UserProfileActivity;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> implements
        View.OnClickListener {
    private final String TAG = "SearchUserAdapter";
    private List<User> userList;
    private ArrayList<User> backupList = new ArrayList<>();
    private Pattern p;
    private Matcher m;
    private Context context;
    private RecyclerView searchResult;

    public SearchUserAdapter(List<User> userList, RecyclerView searchResult, Context context) {
        this.userList = userList;
        this.backupList.addAll(userList);
        this.context = context;
        this.searchResult = searchResult;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = this.userList.get(position);
        //set image
        if (user.getImageFileName() != null) {
            Utility.firebaseStorage.getReference().child("images/" + user.getImageFileName())
                    .getDownloadUrl().addOnSuccessListener(
                    new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context.getApplicationContext()).load(uri)
                                    .into(holder.imageUserAdapter);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, e.getMessage());
                }
            });
        }
        //set name
        holder.usernameUserAdapter.setText(user.getFullName());
    }

    @Override
    public int getItemCount() {
        return this.userList.size();
    }

    @Override
    public void onClick(View v) {
        int itemPosition = searchResult.getChildLayoutPosition(v);
        User user = this.userList.get(itemPosition);
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra("userId", user.getUserId());
        context.startActivity(intent);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView imageUserAdapter;
        private final AppCompatTextView usernameUserAdapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageUserAdapter = itemView.findViewById(R.id.imageUserAdapter);
            usernameUserAdapter = itemView.findViewById(R.id.usernameUserAdapter);
        }

        public CircleImageView getImageUserAdapter() {
            return imageUserAdapter;
        }

        public AppCompatTextView getUsernameUserAdapter() {
            return usernameUserAdapter;
        }
    }

    public void filter(String searchText) {
        this.userList.clear();
        if (searchText.isEmpty()) {
            this.userList.addAll(this.backupList);
        } else {
            p = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
            for (User user : this.backupList) {
                m = p.matcher(user.getFullName());
                if (m.find()) {
                    this.userList.add(user);
                }
            }
        }
        notifyDataSetChanged();

    }
}
