package com.work.project.Model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String status;
    private Date dateOfBirth;
    private String biography;
    private int countryID;
    private int languageID;
    private int genderID;
    // кидаем пользователя в SettingsActivity
    private boolean shouldFinishRegistration = false;
    public static final int GENDER_NOT_SPECIFIED = 0, MALE = 1, FEMALE = 2;

    // полезные методы
    public static DatabaseReference getCurrentUserReference(){
        return getReferenceById(getCurrentUserId());
    }

    public static DatabaseReference getReferenceById(String userId){
        return FirebaseDatabase.getInstance().getReference("Users").child(userId);
    }

    public static String getCurrentUserId(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static DatabaseReference getChatBetween(String userId1, String userId2){
        String[] ids = {userId1, userId2};
        Arrays.sort(ids);
        String encoded = String.format("Chat %s with %s", ids[0], ids[1]);
        return FirebaseDatabase.getInstance().getReference("AllChats").child(encoded);
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // (c) https://stackoverflow.com/a/10215152/6120487
    public int getAge(){
        if(getDateOfBirth() == null)
            return -1;
        Calendar first = getCalendar(getDateOfBirth());
        Calendar second = getCalendar(new Date());
        int diff = second.get(YEAR) - first.get(YEAR);
        if (first.get(MONTH) > second.get(MONTH) ||
                (first.get(MONTH) == second.get(MONTH) && first.get(DATE) > second.get(DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
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

    public boolean isShouldFinishRegistration() {
        return shouldFinishRegistration;
    }

    public void setShouldFinishRegistration(boolean shouldFinishRegistration) {
        this.shouldFinishRegistration = shouldFinishRegistration;
    }
}
