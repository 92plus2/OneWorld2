package com.work.project.Model;

import java.util.Objects;

public class Chat {  // на самом деле сообщение ffs
    private String sender;
    private String receiver;
    private String message;
    private String time;
    private long exactTime;
    private boolean seen;

    public Chat(String sender, String receiver, String message, boolean seen, String time) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.seen = seen;
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

    public long getExactTime() {
        return exactTime;
    }

    public void setExactTime(long exactTime) {
        this.exactTime = exactTime;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean isseen) {
        this.seen = isseen;
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
                Objects.equals(time, otherChat.time) &&
                Objects.equals(exactTime, otherChat.exactTime);
    }
}
