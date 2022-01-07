package com.example.assignment3.post;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment3.R;
import com.example.assignment3.models.Post;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.GlideApp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>
{
    public Context mContext;
    public List<Post> mPost;
    private FirebaseAuth auth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser firebaseUser;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.homepage_display_post, viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Post post = mPost.get(position);
//        GlideApp.with(mContext).load(post.getImgUrl()).into(viewHolder.post_image);
//        viewHolder.username.setText(post.getUidUser());

        if (post.getContent() == null){
            viewHolder.description.setVisibility(View.GONE);
        } else {
            viewHolder.description.setVisibility(View.VISIBLE);
            viewHolder.description.setText(post.getContent());
        }
        viewHolder.publisherInfo(viewHolder.userImage_profile, viewHolder.username, viewHolder.publisher, post);

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        public ImageView userImage_profile, post_image, like, comment, save;
        public TextView username, likes, publisher, description, comments;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            userImage_profile = itemView.findViewById(R.id.large_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
        }

        private void publisherInfo(final ImageView userImage_profile, final TextView username, final TextView publisher, Post post){
            DocumentReference reference = db.collection("users").document(post.getUidUser());
            reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user.getImageFileName()!= null) {
                        StorageReference storageReference = storage.getReference().child("images/" + user.getImageFileName());
                        GlideApp.with(mContext).load(storageReference).into(userImage_profile);
                    }

                    username.setText(user.getFullName());
                    publisher.setText(user.getFullName());
                }
            });
            StorageReference postImgReference = storage.getReference().child(post.getImgUrl());
            GlideApp.with(mContext).load(postImgReference).into(post_image);

        }
    }
}
