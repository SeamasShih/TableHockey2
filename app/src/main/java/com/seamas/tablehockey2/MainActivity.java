package com.seamas.tablehockey2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.seamas.tablehockey2.jbox2d.collision.shapes.CircleShape;
import com.seamas.tablehockey2.jbox2d.collision.shapes.PolygonShape;
import com.seamas.tablehockey2.jbox2d.common.Vec2;
import com.seamas.tablehockey2.jbox2d.dynamics.Body;
import com.seamas.tablehockey2.jbox2d.dynamics.BodyDef;
import com.seamas.tablehockey2.jbox2d.dynamics.BodyType;
import com.seamas.tablehockey2.jbox2d.dynamics.FixtureDef;
import com.seamas.tablehockey2.jbox2d.dynamics.World;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Paint paint = new Paint();
    private float adjY;

    private float rate;
    private World world;

    private float tableWidth;
    private float tableHeight;
    private float edge = 100;

    private Body ball;
    private Body[] balls = new Body[9];
    private Body[] boxes = new Body[6];
    private ArrayList<Integer> fellBalls = new ArrayList<>();

    private InterfaceView view;
    private ValueAnimator animator = ValueAnimator.ofInt(0, 100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        float screenWidth = getResources().getDisplayMetrics().widthPixels - edge * 2;
        float screenHeight = getResources().getDisplayMetrics().heightPixels - edge * 2;
        if (screenWidth * 2 > screenHeight) {
            tableWidth = screenHeight / 2;
            tableHeight = screenHeight;
        } else {
            tableWidth = screenWidth;
            tableHeight = screenWidth * 2;
        }
        rate = tableWidth / HockeyTableSize.innerRectWidth;

        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(HockeyTableSize.ballRadius * rate);
        adjY = (paint.getFontMetrics().descent - paint.getFontMetrics().ascent) / 2 - paint.getFontMetrics().descent;

        createWorld();
        createObject();

        SimpleServiceView serviceView = findViewById(R.id.view);
        serviceView.setCanvasUpdateProcess(new Process());
        serviceView.setFPS(60);

        view = findViewById(R.id.interfaceView);
        view.setLayerHeight(edge * .7f);
        view.setList(fellBalls);
        view.setWhiteBall(ball);
        view.setRate(rate);

        animator.setDuration(1500);
        animator.addListener(new AnimatorListenerAdapter() {
            boolean isCancel = false;

            @Override
            public void onAnimationStart(Animator animation) {
                isCancel = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancel = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCancel)
                    view.setCanHitBall(true);
            }
        });

        Button button = findViewById(R.id.btn);
        button.setOnClickListener(v -> {
            view.startGame();
            v.setVisibility(View.GONE);
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private void createWorld() {
        world = new World(new Vec2(0, 0));
    }

    private void createObject() {
        InitialBallSites initialBallSites = new InitialBallSites(rate);
        for (int i = 0; i < balls.length; i++) {
            createColorBall(i, initialBallSites.x[i], initialBallSites.y[i], HockeyTableSize.ballRadius * rate);
        }
        createBox(0, 0, -tableHeight / 2 - 5, HockeyTableSize.horizontalWallWidth * rate, 10);
        createBox(1, tableWidth / 2 + 5, (-HockeyTableSize.verticalWallHeight / 2 - HockeyTableSize.sidePocketRadius) * rate, 10, (HockeyTableSize.verticalWallHeight) * rate);
        createBox(2, tableWidth / 2 + 5, (HockeyTableSize.verticalWallHeight / 2 + HockeyTableSize.sidePocketRadius) * rate, 10, (HockeyTableSize.verticalWallHeight) * rate);
        createBox(3, 0, tableHeight / 2 + 5, HockeyTableSize.horizontalWallWidth * rate, 10);
        createBox(4, -tableWidth / 2 - 5, (HockeyTableSize.verticalWallHeight / 2 + HockeyTableSize.sidePocketRadius) * rate, 10, (HockeyTableSize.verticalWallHeight) * rate);
        createBox(5, -tableWidth / 2 - 5, (-HockeyTableSize.verticalWallHeight / 2 - HockeyTableSize.sidePocketRadius) * rate, 10, (HockeyTableSize.verticalWallHeight) * rate);

        createWhiteBall(0, tableHeight / 4, HockeyTableSize.ballRadius * rate);
    }

    private void createBox(int i, float x, float y, float w, float h) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w / 2 / rate, h / 2 / rate);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0f;
        fixtureDef.shape = shape;
        fixtureDef.friction = 0;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x / rate, y / rate);
        bodyDef.type = BodyType.STATIC;

        boxes[i] = world.createBody(bodyDef);
        boxes[i].createFixture(fixtureDef);
    }

    public void createColorBall(int i, float x, float y, float radius) {
        CircleShape shape = new CircleShape();
        shape.m_radius = radius / rate;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 4f;
        fixtureDef.restitution = 1f;
        fixtureDef.shape = shape;
        fixtureDef.friction = 0;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x / rate, y / rate);
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.linearDamping = 1f;
        bodyDef.userData = new UserData();

        balls[i] = world.createBody(bodyDef);
        balls[i].createFixture(fixtureDef);
    }

    public void createWhiteBall(float x, float y, float radius) {
        CircleShape shape = new CircleShape();
        shape.m_radius = radius / rate;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 4f;
        fixtureDef.restitution = 1f;
        fixtureDef.shape = shape;
        fixtureDef.friction = 0;

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x / rate, y / rate);
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.linearDamping = 1f;
        bodyDef.userData = new UserData();
        bodyDef.bullet = true;

        ball = world.createBody(bodyDef);
        ball.createFixture(fixtureDef);
    }

    private boolean isInCornerPocket(Vec2 ball) {
        Vec2 v = new Vec2(ball.x - HockeyTableSize.innerRectWidth / 2, ball.y - HockeyTableSize.innerRectHeight / 2);
        if (v.length() < HockeyTableSize.cornerPocketRadius)
            return true;
        v.set(ball.x - HockeyTableSize.innerRectWidth / 2, ball.y + HockeyTableSize.innerRectHeight / 2);
        if (v.length() < HockeyTableSize.cornerPocketRadius)
            return true;
        v.set(ball.x + HockeyTableSize.innerRectWidth / 2, ball.y - HockeyTableSize.innerRectHeight / 2);
        if (v.length() < HockeyTableSize.cornerPocketRadius)
            return true;
        v.set(ball.x + HockeyTableSize.innerRectWidth / 2, ball.y + HockeyTableSize.innerRectHeight / 2);
        return v.length() < HockeyTableSize.cornerPocketRadius;
    }

    private boolean isInSidePocket(Vec2 ball) {
        return ball.x < -HockeyTableSize.innerRectWidth / 2 || ball.x > HockeyTableSize.innerRectWidth / 2;
    }

    private class Process implements CanvasUpdateProcess {
        @Override
        public void onDraw(Canvas canvas) {
            world.step(1f / 60f, 10, 8);
            Vec2 p;
            boolean isMoving = false;

            {
                //draw table
                canvas.drawColor(getResources().getColor(R.color.colorTableHockeyBackground));

                canvas.translate(canvas.getWidth() >> 1, canvas.getHeight() >> 1);
                paint.setColor(getResources().getColor(R.color.colorTableHockeyWood));
                float ballRadius = HockeyTableSize.cornerPocketRadius * rate;
                canvas.drawRoundRect(-tableWidth / 2 - ballRadius, -tableHeight / 2 - ballRadius, tableWidth / 2 + ballRadius, tableHeight / 2 + ballRadius, ballRadius, ballRadius, paint);
                paint.setColor(getResources().getColor(R.color.colorTableHockeyFabric));
                canvas.drawRect(-tableWidth / 2, -tableHeight / 2, tableWidth / 2, tableHeight / 2, paint);

                ballRadius /= Math.sqrt(2);
                paint.setColor(Color.BLACK);
                canvas.drawCircle(-tableWidth / 2, -tableHeight / 2, ballRadius, paint);
                canvas.drawCircle(-tableWidth / 2, +tableHeight / 2, ballRadius, paint);
                canvas.drawCircle(+tableWidth / 2, -tableHeight / 2, ballRadius, paint);
                canvas.drawCircle(+tableWidth / 2, +tableHeight / 2, ballRadius, paint);

                ballRadius = HockeyTableSize.sidePocketRadius * rate;
                canvas.drawArc(-tableWidth / 2 - ballRadius, -ballRadius, -tableWidth / 2 + ballRadius, ballRadius, 90, 180, true, paint);
                canvas.drawArc(tableWidth / 2 - ballRadius, -ballRadius, tableWidth / 2 + ballRadius, ballRadius, -90, 180, true, paint);
            }

            {
                //draw balls
                int[] colors = getResources().getIntArray(R.array.colorTableHockeyBalls);
                for (int i = 0; i < balls.length; i++) {
                    if (!((UserData) balls[i].getUserData()).isDrawing)
                        continue;
                    if (balls[i].getLinearVelocity().length() > 0.000001)
                        isMoving = true;
                    p = balls[i].getPosition();
                    paint.setColor(colors[i]);
                    canvas.drawCircle(p.x * rate, p.y * rate, HockeyTableSize.ballRadius * rate, paint);
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(p.x * rate, p.y * rate, HockeyTableSize.ballRadius * rate / 2, paint);
                    if (i > 7) {
                        canvas.drawArc(p.x * rate - HockeyTableSize.ballRadius * rate, p.y * rate - HockeyTableSize.ballRadius * rate, p.x * rate + HockeyTableSize.ballRadius * rate, p.y * rate + HockeyTableSize.ballRadius * rate, -50, 100, false, paint);
                        canvas.drawArc(p.x * rate - HockeyTableSize.ballRadius * rate, p.y * rate - HockeyTableSize.ballRadius * rate, p.x * rate + HockeyTableSize.ballRadius * rate, p.y * rate + HockeyTableSize.ballRadius * rate, 130, 100, false, paint);
                    }
                    paint.setColor(Color.BLACK);
                    canvas.drawText(String.valueOf(i + 1), p.x * rate, p.y * rate + adjY, paint);
                    if (isInCornerPocket(p) || isInSidePocket(p)) {
                        world.destroyBody(balls[i]);
                        ((UserData) balls[i].getUserData()).isDrawing = false;
                        fellBalls.add(i);
                        view.invalidate();
                    }
                }
                p = ball.getPosition();
                paint.setColor(Color.WHITE);
                canvas.drawCircle(p.x * rate, p.y * rate, HockeyTableSize.ballRadius * rate, paint);
                if (ball.getLinearVelocity().length() > 0.000001)
                    isMoving = true;
                if (isInCornerPocket(p) || isInSidePocket(p)) {
                    //todo lose
                }
            }

            {
                //draw boxes
                paint.setColor(view.isCanHitBall() ? Color.WHITE : Color.RED);
                for (int i = 0; i < boxes.length; i++) {
                    p = boxes[i].getPosition();
                    switch (i) {
                        case 0:
                        case 3:
                            canvas.drawRect(-(p.x + HockeyTableSize.horizontalWallWidth / 2) * rate, p.y * rate - 5, (p.x + HockeyTableSize.horizontalWallWidth / 2) * rate, p.y * rate + 5, paint);
                            break;
                        case 1:
                        case 2:
                        case 4:
                        case 5:
                            canvas.drawRect(p.x * rate - 5, (p.y - HockeyTableSize.verticalWallHeight / 2) * rate, p.x * rate + 5, (p.y + HockeyTableSize.verticalWallHeight / 2) * rate, paint);
                            break;
                    }
                }
            }

            if (isMoving) {
                if (animator.isRunning())
                    runOnUiThread(() -> animator.cancel());
            } else if (!animator.isRunning()) {
                runOnUiThread(() -> animator.cancel());
                runOnUiThread(() -> animator.start());
            }
        }
    }
}
