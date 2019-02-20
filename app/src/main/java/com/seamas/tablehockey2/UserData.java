package com.seamas.tablehockey2;

public class UserData {
    public boolean isDrawing = true;
    public int order = 0;

    public UserData(int order){
        this.order = order;
    }

    public void set(UserData userData){
        isDrawing = userData.isDrawing;
        order = userData.order;
    }
}
