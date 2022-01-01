package com.example.assignment3.utilities;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.assignment3.models.ChatRoom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class Utility {
    private static final String TAG = "Utility";
    private static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private static FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    //convert image to byte array
    static public byte[] convertImageToArrayByte(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG,
                100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    //genUID combine between 2 uID of users.
    static public String genUID(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + uid2;
        } else {
            return uid2 + uid1;
        }
    }

    /**
     * Auto generate chat room when a user follows other.
     * */

    static public void autoCreateRoomChat(String roomID, String authorID){
        firebaseFirestore.collection("chats")
                .whereEqualTo("roomID", roomID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                ChatRoom chatRoom = new ChatRoom(roomID, Arrays.asList(currentUser.getUid(), authorID));
                                firebaseFirestore.collection("chats")
                                        .document(roomID)
                                        .set(chatRoom)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.v(TAG, "New room chat ID: " + roomID);
                                                }
                                            }
                                        });

                            } else {
                                Log.v(TAG, "Room chat ID: " + task.getResult().size());
                                /**
                                 * The app surely that the query only return 1 document.
                                 * Therefore, the app will get the first document.
                                 * */
                            }
                        }
                    }
                });
    }
}
