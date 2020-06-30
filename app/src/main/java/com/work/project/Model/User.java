package com.work.project.Model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String search;

    public User(String id, String username, String imageURL, String status, String search) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public static DatabaseReference getChatBetween(String userId1, String userId2){
        String[] ids = {userId1, userId2};
        Arrays.sort(ids);
        String encoded = String.format("Chat %s with %s", ids[0], ids[1]);
        return FirebaseDatabase.getInstance().getReference("AllChats").child(encoded);
    }

    /*public static void getAllChatsFor(String userId){
        final DatabaseReference ourChats = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userId);
        ourChats.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    String sender = chatlist.getId();
                    sender
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }*/
}
