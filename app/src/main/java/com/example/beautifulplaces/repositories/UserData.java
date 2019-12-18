package com.example.beautifulplaces.repositories;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class UserData {
    private static FirebaseAuth INSTANCE = FirebaseAuth.getInstance();

    private UserData(){}

    public static FirebaseUser getUser(){
        return INSTANCE.getCurrentUser();
    }
}
