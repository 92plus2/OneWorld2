package com.work.project.Model;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.util.*;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private String time;
    private boolean isseen;

    public Chat(String sender, String receiver, String message, boolean isseen, String time) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.time = time;
    }

    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {

        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    @Override
    public int hashCode() {
        return message == null? 0 : message.hashCode();
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof Chat))
            return false;
        Chat otherChat = (Chat) other;
        return Objects.equals(sender, otherChat.sender) &&
                Objects.equals(receiver, otherChat.receiver) &&
                Objects.equals(message, otherChat.message) &&
                Objects.equals(time, otherChat.time);
    }
}
