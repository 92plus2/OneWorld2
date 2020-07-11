package com.work.project.Notifications;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.work.project.MainActivity;
import com.work.project.MessageActivity;
import com.work.project.R;

import java.util.Map;

public class Data {
    private String userId;
    private String username;
    private String message;
    public static final int NEW_MESSAGE = 0, NEW_FRIEND_REQUEST = 1, FRIEND_REQUEST_ACCEPTED = 2;
    private int notificationType;

    public Data(String userId, String username, String message, int notificationType) {
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.notificationType = notificationType;
    }

    public Data(Map<String, String> data){
        this(data.get("userId"), data.get("username"), data.get("message"), Integer.parseInt(data.get("notificationType")));
    }

    public String getTitle(Resources res){
        switch(notificationType){
            case NEW_MESSAGE:
                return res.getString(R.string.new_message);
            case NEW_FRIEND_REQUEST:
                return res.getString(R.string.friend_request);
            case FRIEND_REQUEST_ACCEPTED:
                return res.getString(R.string.friend_request_accepted);
            default:
                return null;
        }
    }

    public String getBody(Resources res){
        switch (notificationType){
            case NEW_MESSAGE:
                return res.getString(R.string.username_and_message_format, username, message);
            case NEW_FRIEND_REQUEST:
                return res.getString(R.string.you_were_liked_by_username, username);
            case FRIEND_REQUEST_ACCEPTED:
                return res.getString(R.string.your_friend_request_was_accepted_by_username, username);
            default:
                return null;
        }
    }

    public Intent getIntent(Context context){
        if(notificationType == NEW_MESSAGE || notificationType == FRIEND_REQUEST_ACCEPTED) {
            // посылаем в диалог с пользователем
            Intent intent = new Intent(context, MessageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("userid", userId);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            return intent;
        }
        else {
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            return intent;
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }
}
