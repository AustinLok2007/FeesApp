package com.example.test2;

import com.google.firebase.firestore.DocumentId;

public class User {

    @DocumentId
    private String uid;
    private String email;

    public User() {
    }

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
