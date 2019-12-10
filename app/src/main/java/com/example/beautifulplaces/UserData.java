package com.example.beautifulplaces;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserData {
    public static FirebaseUser getUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }
}
