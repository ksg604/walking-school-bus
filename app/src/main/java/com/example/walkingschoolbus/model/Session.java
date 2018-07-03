package com.example.walkingschoolbus.model;

public class Session {
    private String token;

    public void setToken(String string){
        this.token = string;
    }

    public String getToken(){
        return token;
    }

    public void deleteToken(){
        this.token = null;
    }


}

