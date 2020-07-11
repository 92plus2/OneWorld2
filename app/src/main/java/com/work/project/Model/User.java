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
    private int countryID;
    private int languageID;
    private int genderID;
    public static final int GENDER_NOT_SPECIFIED = 0, MALE = 1, FEMALE = 2;

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

    public int getCountryID() {
        return countryID;
    }

    public void setCountryID(int countryID) {
        this.countryID = countryID;
    }

    public int getLanguageID() {
        return languageID;
    }

    public void setLanguageID(int languageID) {
        this.languageID = languageID;
    }

    public int getGenderID() {
        return genderID;
    }

    public void setGenderID(int genderID) {
        this.genderID = genderID;
    }

    public static DatabaseReference getChatBetween(String userId1, String userId2){
        String[] ids = {userId1, userId2};
        Arrays.sort(ids);
        String encoded = String.format("Chat %s with %s", ids[0], ids[1]);
        return FirebaseDatabase.getInstance().getReference("AllChats").child(encoded);
    }

    //     public static final int RU = 0, EN = 1, DE = 2, ES = 3, FR = 4, IT = 5, ZH = 6;

}
