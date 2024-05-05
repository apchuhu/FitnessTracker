package com.example.fitnesstracker;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Random;

@IgnoreExtraProperties
public class User {

    public String userId;
    public String username;
    public String email;


    // Pass a custom Java object, if the class that defines it
    // has a default constructor that takes no arguments
    // and has public getters for the properties to be assigned.
    public User(String username, String email) {
        setUsername(username);
        setEmail(email);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User(String userId, String username, String email) {
        setUserId(userId);
        setUsername(username);
        setEmail(email);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Method that should create a new users randomized userID and check to make
    // sure that it is not the same as any other userID in the database.
    public static String createNewUserId() {
//        ValueEventListener
        Random random = new Random();
        Integer max = 99999;
        Integer min = 0;
        String userID = "N/A";
//        String userID =
//
//        while((!userID.equals("N/A") && ))
        userID = String.valueOf(Math.abs(random.nextInt(max - min + 1) + min));
        return userID;

    }
}