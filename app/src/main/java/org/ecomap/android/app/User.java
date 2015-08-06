package org.ecomap.android.app;

/**
 * Created by Stanislav on 06.08.2015.
 */
public class User {
    private static User user;

    private static String firstName, lastName, email, password, role;

    private User(String firstName, String lastName, String email, String password, String role){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    //getInstance method that initialize and return new user
    public static User getInstance(String firstName, String lastName, String email, String password, String role){

        if (user == null){
            user = new User(firstName, lastName, email, password, role);
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
}
