package com.example.myapplication;

/**
 * Created by Domirae on 2018-03-23.
 */

public class ChatMessage {
    public boolean right;
    public String message;

    public ChatMessage(boolean right, String message) {
        super();
        this.right = right;
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
