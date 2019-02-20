package com.seamas.tablehockey2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.seamas.tablehockey2.jbox2d.common.Vec2;
import com.seamas.tablehockey2.jbox2d.dynamics.Body;

import java.util.ArrayList;

public class InterfaceView extends View {
    private float layerHeight = 0f;
    private Body ball;
    private ArrayList<Integer> list = new ArrayList<>();
    private float ballRadius = 0f;
    private Paint paint = new Paint();
    private float adjY = 0;
    private boolean canHitBall = true;
    private Vec2 finger = new Vec2();
    private Vec2 move = new Vec2();
    private float rate;
    private boolean is1P = true;
    private int listNum = 0;

    private enum MODE {
        START,
        FREE,
        NORMAL,
        AIMING,
        END
    }

    private MODE mode = MODE.START;

    public InterfaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    public void setLayerHeight(float layerHeight) {
        this.layerHeight = layerHeight;
        ballRadius = layerHeight / 2;

        paint.setTextSize(ballRadius);
        adjY = (paint.getFontMetrics().descent - paint.getFontMetrics().ascent) / 2 - paint.getFontMetrics().descent;
        invalidate();
    }

    public void setWhiteBall(Body ball) {
        this.ball = ball;
    }

    public void setList(ArrayList<Integer> list) {
        this.list = list;
        invalidate();
    }

    public void gameStart() {
        mode = MODE.NORMAL;
    }

    public void gameOver() {
        mode = MODE.END;
    }

    public void gamePrepare() {
        mode = MODE.START;
    }

    public void gameFreeMode() {
        mode = MODE.FREE;
    }

    public boolean isGaming() {
        return mode == MODE.NORMAL || mode == MODE.FREE;
    }

    public void setIs1P(boolean is1P) {
        this.is1P = is1P;
    }

    public boolean isIs1P() {
        return is1P;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mode == MODE.START) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    float x = (event.getX() - (getWidth() >> 1)) / rate;
                    float y = (event.getY() - (getHeight() >> 1)) / rate;
                    if (x < -SnookerSize.innerRectWidth / 2 + SnookerSize.ballRadius)
                        x = -SnookerSize.innerRectWidth / 2 + SnookerSize.ballRadius;
                    else if (x > SnookerSize.innerRectWidth / 2 - SnookerSize.ballRadius)
                        x = SnookerSize.innerRectWidth / 2 - SnookerSize.ballRadius;
                    if (y < SnookerSize.innerRectHeight / 4 + SnookerSize.ballRadius)
                        y = SnookerSize.innerRectHeight / 4 + SnookerSize.ballRadius;
                    else if (y > SnookerSize.innerRectHeight / 2 - SnookerSize.ballRadius)
                        y = SnookerSize.innerRectHeight / 2 - SnookerSize.ballRadius;
                    ball.setTransform(new Vec2(x, y), 0);
                    break;
            }
        } else if (mode == MODE.END) {

        } else if (mode == MODE.FREE) {
            float x = (event.getX() - (getWidth() >> 1)) / rate;
            float y = (event.getY() - (getHeight() >> 1)) / rate;
            if (x < -SnookerSize.innerRectWidth / 2 + SnookerSize.ballRadius)
                x = -SnookerSize.innerRectWidth / 2 + SnookerSize.ballRadius;
            else if (x > SnookerSize.innerRectWidth / 2 - SnookerSize.ballRadius)
                x = SnookerSize.innerRectWidth / 2 - SnookerSize.ballRadius;
            if (y < -SnookerSize.innerRectHeight / 2 + SnookerSize.ballRadius)
                y = -SnookerSize.innerRectHeight / 2 + SnookerSize.ballRadius;
            else if (y > SnookerSize.innerRectHeight / 2 - SnookerSize.ballRadius)
                y = SnookerSize.innerRectHeight / 2 - SnookerSize.ballRadius;
            ball.setTransform(new Vec2(x, y), 0);
        } else if (canHitBall) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    finger.set(event.getX(), event.getY());
                    move.setZero();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mode = MODE.AIMING;
                    move.set(event.getX() - finger.x, event.getY() - finger.y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    if (move.length() <= 600) {
                        listNum = list.size();
                        ((MainActivity) getContext()).HitWhiteBall(move.mul(-1f / 50f));
                        canHitBall = false;
                    } else if (move.length() <= 800) {
                        listNum = list.size();
                        ((MainActivity) getContext()).HitWhiteBall(move.mul(-12 / move.length()));
                        canHitBall = false;
                    }
                    mode = MODE.NORMAL;
                    invalidate();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mode = MODE.NORMAL;
                    invalidate();
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int[] colors = getResources().getIntArray(R.array.colorTableHockeyBalls);

        canvas.save();
        canvas.translate(ballRadius, ballRadius);
        for (int i = 0; i < list.size(); i++) {
            int order = list.get(i);
            paint.setColor(colors[order]);
            canvas.drawCircle(0, 0, ballRadius, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(0, 0, ballRadius / 2, paint);
            if (i > 7) {
                canvas.drawArc(-ballRadius, -ballRadius, ballRadius, ballRadius, -50, 100, false, paint);
                canvas.drawArc(-ballRadius, -ballRadius, ballRadius, ballRadius, 130, 100, false, paint);
            }
            paint.setColor(Color.BLACK);
            canvas.drawText(String.valueOf(order + 1), 0, 0 + adjY, paint);
            canvas.translate(ballRadius * 2, 0);
        }
        canvas.restore();

        canvas.save();
        canvas.translate(getWidth() >> 1, getHeight() - ballRadius * 1.5f);
        paint.setColor(is1P ? Color.YELLOW : Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.scale(2, 2);
        canvas.drawRect(-ballRadius, -ballRadius * .5f, ballRadius, ballRadius * .5f, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(is1P ? "1P" : "2P", 0, adjY, paint);
        canvas.restore();

        switch (mode) {
            case AIMING:
                canvas.translate(getWidth() >> 1, getHeight() >> 1);
                Vec2 p = ball.getPosition();
                if (move.length() <= 600) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3);
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(p.x * rate, p.y * rate, ballRadius + move.length() / 5, paint);
                    paint.setColor(is1P ? Color.YELLOW : Color.BLUE);
                    canvas.drawLine(p.x * rate + move.mul(ballRadius / move.length() + 0.2f).x, p.y * rate + move.mul(ballRadius / move.length() + 0.2f).y, p.x * rate + move.mul(ballRadius / move.length()).x, p.y * rate + move.mul(ballRadius / move.length()).y, paint);
                    paint.setStyle(Paint.Style.FILL);
                } else if (move.length() <= 800) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3);
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(p.x * rate, p.y * rate, ballRadius + 120, paint);
                    paint.setColor(is1P ? Color.YELLOW : Color.BLUE);
                    canvas.drawLine(p.x * rate + move.mul((ballRadius + 120) / move.length()).x, p.y * rate + move.mul((ballRadius + 120) / move.length()).y, p.x * rate + move.mul(ballRadius / move.length()).x, p.y * rate + move.mul(ballRadius / move.length()).y, paint);
                    paint.setStyle(Paint.Style.FILL);
                } else {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.RED);
                    paint.setStrokeWidth(3);
                    canvas.drawCircle(p.x * rate, p.y * rate, ballRadius + 160, paint);
                    paint.setStyle(Paint.Style.FILL);
                }
                break;
        }
    }

    public boolean isCanHitBall() {
        return canHitBall;
    }

    public void setCanHitBall(boolean canHitBall) {
        this.canHitBall = canHitBall;
        if (listNum == list.size())
            is1P = !is1P;
        invalidate();
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
}
