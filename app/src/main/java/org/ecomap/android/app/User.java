package org.ecomap.android.app;

import android.util.Log;


import org.ecomap.android.app.utils.UserSubscriber;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class User {
    private static User user;

    private static String firstName, lastName, email, password, role;
    private static int userId;
    private static Set<String> set;
    private static final String LOG_TAG="test";

    private static final int NONE = 0;
    private static final int OWN = 1;
    private static final int ALL = 2;

    private static Set<UserSubscriber> subscribers;




    private User(String firstName, String lastName, String email, String password, String role, String userId, Set<String> set){
        User.firstName = firstName;
        User.lastName = lastName;
        User.email = email;
        User.password = password;
        User.role = role;
        User.userId = Integer.valueOf(userId);
        User.set = set;
        if(subscribers==null){
        subscribers=new HashSet<UserSubscriber>();}
        else{ notifySubscribers();}

    }

    //getInstance method that initialize and return new user
    public static User getInstance(String firstName, String lastName, String email, String password, String role, String userId, Set<String> set){

        if (user == null){
            user = new User(firstName, lastName, email, password, role, userId, set);

        }
        else{
            notifySubscribers();

        }

        return user;
    }

    //getInstance method that return current user
    public static User getInstance(){
        notifySubscribers();
        return user;
    }

    public static String getFirstName(){
        return firstName;
    }

    public static String getLastName(){
        return lastName;
    }

    public static String getEmail(){
        return email;
    }

    public static String getPassword(){
        return password;
    }

    public static String getRole(){
        return role;
    }

    public static Integer getUserId(){
        return userId;
    }

    //User class and UserSubscriber interface implement Observer pattern. When userInstance is created it will notify all the subscribers that means User logged in.
    //To implement this pattern correctly in your fragment, first implement the interface, then call addSubscriber when created or resumed your fragment and removeSubscriber when stopped or detached.
    //You will also need to have a refference on user instance, so call getInstance() with nullable check.

    public  static void addSubscriber(UserSubscriber sub){
        if(subscribers!=null)subscribers.add(sub);
    }

    public static void removeSubscriber(UserSubscriber sub){
        if(subscribers!=null)subscribers.remove(sub);
    }
    private static void notifySubscribers(){
        Log.i(LOG_TAG, "subscribers are notified");
        for(UserSubscriber sub:subscribers){
            sub.changeState();
        }
    }

    public static Set<String> getSetFromJSONArray(JSONArray jArr){
        Set<String> set = new HashSet<>();

        for (int i = 0; i < jArr.length(); i++){
            try {
                set.add(jArr.getString(i));
            } catch (JSONException e){
                Log.e(User.class.getSimpleName(), "JSON exception when looping through JSONArray");
            }
        }

        return set;
    }

    public static boolean canUserDeleteProblem(Problem p){

        if (user != null) {
            for (String s : set) {
                if (s.contains("ProblemHandler:DELETE")) {
                    if (userId == p.userId) {
                        return (s.contains("OWN") || s.contains("ANY"));
                    } else {
                        return s.contains("ANY");
                    }
                }
            }
        }

        return false;
    }
    public static boolean canUserDeleteComment(int whoseComment){
        if(user!=null){
            for(String s: set){
                if(s.contains("CommentHandler:DELETE")){
                    if(userId==whoseComment)
                    {
                        return (s.contains("OWN") || s.contains("ANY"));
                    }
                    else
                    {
                        return s.contains("ANY");
                    }

                }
            }
        }
        return false;
    }

    public static boolean canUserEditProblem(Problem p){
        if (user != null) {
            for (String s : set) {
                if (s.contains("ProblemHandler:PUT")) {
                    if (userId == p.userId) {
                        return (s.contains("OWN") || s.contains("ANY"));
                    } else {
                        return s.contains("ANY");
                    }
                }
            }
        }

        return false;
    }

    public static void deleteUserInstance(){
        user = null;
        notifySubscribers();

    }



}
