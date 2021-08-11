package com.moalzoabi.chathub;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private String time;
    private boolean isSeen;


    public Chat (String sender, String receiver, String message, boolean isSeen, String time){
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
        this.time = time;
    }

    public Chat(){
    }

    public void setSender(String sender){
        this.sender = sender;
    }
    public void setReceiver(String receiver){
        this.receiver = receiver;
    }
    public void setMessage(String message){
        this.message = message;
    }
    public void setSeen(boolean seen) {
        isSeen = seen;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }
    public String getSender(){
        return sender;
    }
    public String getReceiver(){
        return receiver;
    }
    public String getMessage(){
        return message;
    }
    public boolean getIsSeen() {
        return isSeen;
    }
}
