package com.example.beautifulplaces.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beautifulplaces.repositories.UserData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragmentViewModel extends ViewModel {

    private MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    public MutableLiveData<FirebaseUser> getUser() {
        return user;
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    public void loadUser(){
        user.setValue(UserData.getUser());
    }

}
