package com.moalzoabi.chathub;

public class Users {

    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String typing;
    private String bio;

    public Users(){
    }

    public Users(String id, String username, String imageURL, String status, String typing, String bio) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.typing = typing;
        this.bio = bio;
    }

    public String getId(){
        return id;
    }

    public String getUsername(){
        return username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getStatus(){
        return status;
    }

    public String getTyping() {
        return typing;
    }

    public String getBio() {
        return bio;
    }
}
