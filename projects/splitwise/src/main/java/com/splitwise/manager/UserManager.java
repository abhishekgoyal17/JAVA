package com.splitwise.manager;


import com.splitwise.model.User;

import java.util.HashMap;
import java.util.Map;

//Singleton
public class UserManager {

    private static UserManager instance;
    private Map<String, User> users;

    private UserManager(){
        users=new HashMap<>();
    }

    public static UserManager getInstance(){
        if(instance==null){
            instance=new UserManager();
        }
        return  instance;
    }

    public void  addUser(User user){
        users.put(user.getUserId(),user);
    }
    public User getUser(String userId){
       return  users.get((userId));
    }
}
