package com.aka.assignment.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by akshayaggarwal99 on 17-02-2017.
 */

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String photoUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String photoUrl) {
        this.username = username;
        this.email = email;
        this.photoUrl=photoUrl;
    }

}
// [END blog_user_class]