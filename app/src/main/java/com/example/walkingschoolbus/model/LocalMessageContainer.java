package com.example.walkingschoolbus.model;
import android.content.Context;

public class LocalMessageContainer {

    private String messageContent;
    private static LocalMessageContainer instance;

    public static LocalMessageContainer getInstance(){
        if(instance == null){
            instance = new LocalMessageContainer();
        }
        return instance;
    }
    private LocalMessageContainer(){}

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}
