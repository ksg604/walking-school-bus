package com.example.walkingschoolbus.model;

public class UserInfo {
    private String name;
    private String email;
    private String password;


    /*
        Singleton Support
     */
    private static UserInfo instance;

    private UserInfo () {

    }

    public UserInfo(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }


    public static UserInfo getInstance() {
        if(instance == null) {
            instance = new UserInfo();
        }
        return instance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

