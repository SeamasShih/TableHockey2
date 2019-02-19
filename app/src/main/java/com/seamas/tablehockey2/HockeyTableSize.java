package com.seamas.tablehockey2;

public class HockeyTableSize {
    public static float outerRectWidth = 1.32f;
    public static float outerRectHeight = 2.59f;
    public static float innerRectWidth = 1.27f;
    public static float innerRectHeight = 2.54f;
    public static float sidePocketRadius = 0.0911225f;
    public static float cornerPocketRadius = 0.12f;
    public static float ballRadius = cornerPocketRadius / 2 - 0.013f;

    public static float horizontalWallWidth = innerRectWidth - cornerPocketRadius / (float) Math.sqrt(2) * 2;
    public static float verticalWallHeight = (innerRectHeight - (cornerPocketRadius / (float) Math.sqrt(2) * 2) - sidePocketRadius * 2) / 2;
}
