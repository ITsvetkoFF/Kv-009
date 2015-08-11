package org.ecomap.android.app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Stanislav on 06.08.2015.
 */
public class User {
    private static User user;

    private static String firstName, lastName, email, password, role;
    private static int userId;
    private static Set<String> set;

    private static final int NONE = 0;
    private static final int OWN = 1;
    private static final int ALL = 2;

    private User(String firstName, String lastName, String email, String password, String role, String userId, Set<String> set){
        User.firstName = firstName;
        User.lastName = lastName;
        User.email = email;
        User.password = password;
        User.role = role;
        User.userId = Integer.valueOf(userId);
        User.set = set;
    }

    //getInstance method that initialize and return new user
    public static User getInstance(String firstName, String lastName, String email, String password, String role, String userId, Set<String> set){

        if (user == null){
            user = new User(firstName, lastName, email, password, role, userId, set);
        }

        return user;
    }

    //getInstance method that return current user
    public static User getInstance(){
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
}
