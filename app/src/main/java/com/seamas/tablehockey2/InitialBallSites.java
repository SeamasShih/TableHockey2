package com.seamas.tablehockey2;

public class InitialBallSites {
    public float[] x = new float[9], y = new float[9];

    public InitialBallSites(float rate) {
        float r = HockeyTableSize.ballRadius * 2 * rate;
        float cx = 0;
        float cy = -HockeyTableSize.innerRectHeight / 4 * rate;
        x[0] = cx;
        y[0] = cy + r * (float) Math.sqrt(3);

        x[2] = cx - r;
        y[2] = cy;

        x[4] = cx;
        y[4] = cy - r * (float) Math.sqrt(3);

        x[6] = cx + r;
        y[6] = cy;

        x[1] = (x[0] + x[2]) / 2;
        y[1] = (y[0] + y[2]) / 2;

        x[3] = (x[2] + x[4]) / 2;
        y[3] = (y[2] + y[4]) / 2;

        x[5] = (x[4] + x[6]) / 2;
        y[5] = (y[4] + y[6]) / 2;

        x[7] = (x[6] + x[0]) / 2;
        y[7] = (y[6] + y[0]) / 2;

        x[8] = cx;
        y[8] = cy;
    }
}
