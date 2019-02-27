package com.vermolen.uurrooster.Classes;

import com.vermolen.uurrooster.Model.User;

public class UserSingleton {
    private static User user;

    public static void setInstance(User user){
        UserSingleton.user = user;
    }

    public static User getInstance(){
        return user;
    }
}
