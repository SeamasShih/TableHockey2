package com.seamas.tablehockey2;

import com.seamas.tablehockey2.jbox2d.common.Vec2;

public class RecoverStatus {
    public Vec2[] positions = new Vec2[9];
    public Vec2 whitePosition;
    public UserData[] userData = new UserData[9];
    public UserData whiteUserData;

    public RecoverStatus() {
        for (int i = 0; i < positions.length; i++) {
            positions[i] = new Vec2();
            userData[i] = new UserData();
        }
        whitePosition = new Vec2();
        whiteUserData = new UserData();
    }
}
