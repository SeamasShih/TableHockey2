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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.seamas.tablehockey2.jbox2d.callbacks.ContactImpulse;
import com.seamas.tablehockey2.jbox2d.callbacks.ContactListener;
import com.seamas.tablehockey2.jbox2d.collision.Manifold;
import com.seamas.tablehockey2.jbox2d.collision.shapes.CircleShape;
import com.seamas.tablehockey2.jbox2d.collision.shapes.PolygonShape;
import com.seamas.tablehockey2.jbox2d.common.Vec2;
import com.seamas.tablehockey2.jbox2d.dynamics.Body;
import com.seamas.tablehockey2.jbox2d.dynamics.BodyDef;
import com.seamas.tablehockey2.jbox2d.dynamics.BodyType;
import com.seamas.tablehockey2.jbox2d.dynamics.FixtureDef;
import com.seamas.tablehockey2.jbox2d.dynamics.World;
import com.seamas.tablehockey2.jbox2d.dynamics.contacts.Contact;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Paint paint = new Paint();
    private float adjY;

    private float rate;
    private World world;
    private boolean firstTouch = true;
    private boolean isLegalHit = true;
    private RecoverStatus recoverStatus = new RecoverStatus();
    private int minBall = 0;

    private float tableWidth;
    private float tableHeight;
    private float edge = 100;

    private Body ball;
    private Body[] balls = new Body[9];
    private Body[] boxes = new Body[6];
    private ArrayList<Integer> fellBalls = new ArrayList<>();

    private InterfaceView view;
    private ValueAnimator animator = ValueAnimator.ofInt(0, 100);
    private Button button;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        float screenWidth = getResources().getDisplayMetrics().widthPixels - edge * 2;
        float screenHeight = getResources().getDisplayMetrics().heightPixels - edge * 2;
        screenWidth = screenWidth * SnookerSize.innerRectWidth / (SnookerSize.innerRectWidth + SnookerSize.cornerPocketRadius * (float) Math.sqrt(2));
        screenHeight = screenHeight * SnookerSize.innerRectHeight / (SnookerSize.innerRectHeight + SnookerSize.cornerPocketRadius * (float) Math.sqrt(2));
        if (screenWidth * 2 > screenHeight) {
            tableWidth = screenHeight / 2;
            tableHeight = screenHeight;
        } else {
            tableWidth = screenWidth;
            tableHeight = screenWidth * 2;
        }

        rate = tableWidth / SnookerSize.innerRectWidth;

        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(SnookerSize.ballRadius * rate);
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
            boolean isStart = false;

            @Override
            public void onAnimationStart(Animator animation) {
                isCancel = false;
                isStart = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancel = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCancel && isStart)
                    view.setCanHitBall(true);
                isStart = false;
                firstTouch = true;
            }
        });

        button = findViewById(R.id.btn);
        button.setOnClickListener(v -> {
            view.gameStart();
            v.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        });

        textView = findViewById(R.id.message);
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

    public void HitWhiteBall(Vec2 force) {
        saveGameStatus();
        ball.applyForceToCenter(force);
        firstTouch = true;
    }

    private void restartGame() {
        InitialBallSites initialBallSites = new InitialBallSites(rate);
        for (int i = 0; i < balls.length; i++) {
            world.destroyBody(balls[i]);
            createColorBall(i, initialBallSites.x[i], initialBallSites.y[i], SnookerSize.ballRadius * rate);
        }
        world.destroyBody(ball);
        createWhiteBall(0, tableHeight / 4, SnookerSize.ballRadius * rate);
        fellBalls.clear();
        view.setWhiteBall(ball);
        view.invalidate();
        isLegalHit = true;
    }

    private void saveGameStatus() {
        for (int i = 0; i < balls.length; i++) {
            recoverStatus.positions[i].set(balls[i].getPosition());
            recoverStatus.userData[i] = ((UserData) balls[i].getUserData());
        }
        recoverStatus.whitePosition.set(ball.getPosition());
        recoverStatus.whiteUserData = ((UserData) ball.getUserData());
        recoverStatus.balls.clear();
        recoverStatus.balls.addAll(fellBalls);
    }

    private void recoverGame() {
        for (int i = 0; i < balls.length; i++) {
            world.destroyBody(balls[i]);
            createColorBall(i, recoverStatus.positions[i].x, recoverStatus.positions[i].y, SnookerSize.ballRadius * rate);
            balls[i].setUserData(recoverStatus.userData[i]);
        }
        world.destroyBody(ball);
        createWhiteBall(recoverStatus.whitePosition.x, recoverStatus.whitePosition.y, SnookerSize.ballRadius * rate);
        ball.setUserData(recoverStatus.whiteUserData);
        view.setWhiteBall(ball);
        fellBalls.clear();
        fellBalls.addAll(recoverStatus.balls);
        isLegalHit = true;
        view.invalidate();
    }

    private void createWorld() {
        world = new World(new Vec2(0, 0));
        world.setContactListener(new ContactListener() {

            @Override
            public void beginContact(Contact contact) {
                if (firstTouch && view.isGaming() && contact.m_fixtureA.getBody().getUserData() != null && contact.m_fixtureB.getBody().getUserData() != null) {
                    Log.d("Seamas", "a = " + ((UserData) contact.m_fixtureA.getBody().getUserData()).order + " b = " + ((UserData) contact.m_fixtureB.getBody().getUserData()).order);
                    if ((contact.m_fixtureA.getBody() != balls[minBall] || contact.m_fixtureB.getBody() != ball) &&
                            (contact.m_fixtureB.getBody() != balls[minBall] || contact.m_fixtureA.getBody() != ball)) {
                        isLegalHit = false;
                        runOnUiThread(() -> {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            recoverGame();
                        });
                    }
                    firstTouch = false;
                }
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    private void createObject() {
        InitialBallSites initialBallSites = new InitialBallSites(rate);
        for (int i = 0; i < balls.length; i++) {
            createColorBall(i, initialBallSites.x[i], initialBallSites.y[i], SnookerSize.ballRadius * rate);
        }
        createBox(0, 0, -tableHeight / 2 - 5, SnookerSize.horizontalWallWidth * rate, 10);
        createBox(1, tableWidth / 2 + 5, (-SnookerSize.verticalWallHeight / 2 - SnookerSize.sidePocketRadius) * rate, 10, (SnookerSize.verticalWallHeight) * rate);
        createBox(2, tableWidth / 2 + 5, (SnookerSize.verticalWallHeight / 2 + SnookerSize.sidePocketRadius) * rate, 10, (SnookerSize.verticalWallHeight) * rate);
        createBox(3, 0, tableHeight / 2 + 5, SnookerSize.horizontalWallWidth * rate, 10);
        createBox(4, -tableWidth / 2 - 5, (SnookerSize.verticalWallHeight / 2 + SnookerSize.sidePocketRadius) * rate, 10, (SnookerSize.verticalWallHeight) * rate);
        createBox(5, -tableWidth / 2 - 5, (-SnookerSize.verticalWallHeight / 2 - SnookerSize.sidePocketRadius) * rate, 10, (SnookerSize.verticalWallHeight) * rate);

        createWhiteBall(0, tableHeight / 4, SnookerSize.ballRadius * rate);
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
        bodyDef.userData = new UserData(i);
        bodyDef.bullet = true;

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
        bodyDef.userData = new UserData(-1);
        bodyDef.bullet = true;

        ball = world.createBody(bodyDef);
        ball.createFixture(fixtureDef);
    }

    private boolean isInCornerPocket(Vec2 ball) {
        Vec2 v = new Vec2(ball.x - SnookerSize.innerRectWidth / 2, ball.y - SnookerSize.innerRectHeight / 2);
        if (v.length() < SnookerSize.cornerPocketRadius)
            return true;
        v.set(ball.x - SnookerSize.innerRectWidth / 2, ball.y + SnookerSize.innerRectHeight / 2);
        if (v.length() < SnookerSize.cornerPocketRadius)
            return true;
        v.set(ball.x + SnookerSize.innerRectWidth / 2, ball.y - SnookerSize.innerRectHeight / 2);
        if (v.length() < SnookerSize.cornerPocketRadius)
            return true;
        v.set(ball.x + SnookerSize.innerRectWidth / 2, ball.y + SnookerSize.innerRectHeight / 2);
        return v.length() < SnookerSize.cornerPocketRadius;
    }

    private boolean isInSidePocket(Vec2 ball) {
        return ball.x < -SnookerSize.innerRectWidth / 2 || ball.x > SnookerSize.innerRectWidth / 2;
    }

    private void freeMode() {
        recoverGame();
        button.setVisibility(View.VISIBLE);
        button.setText("START");
        button.setOnClickListener(v -> {
            view.gameStart();
            v.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        });
        view.gameFreeMode();

        textView.setVisibility(View.VISIBLE);
        textView.setText("FREE BALL");
    }

    private void defineMinBall() {
        for (int i = 0; i < balls.length; i++) {
            if (!fellBalls.contains(i)) {
                minBall = i;
                break;
            }
        }
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
                float ballRadius = SnookerSize.cornerPocketRadius * rate;
                canvas.drawRoundRect(-tableWidth / 2 - ballRadius, -tableHeight / 2 - ballRadius, tableWidth / 2 + ballRadius, tableHeight / 2 + ballRadius, ballRadius, ballRadius, paint);
                paint.setColor(getResources().getColor(R.color.colorTableHockeyFabric));
                canvas.drawRect(-tableWidth / 2, -tableHeight / 2, tableWidth / 2, tableHeight / 2, paint);

                ballRadius /= Math.sqrt(2);
                paint.setColor(Color.BLACK);
                canvas.drawCircle(-tableWidth / 2, -tableHeight / 2, ballRadius, paint);
                canvas.drawCircle(-tableWidth / 2, +tableHeight / 2, ballRadius, paint);
                canvas.drawCircle(+tableWidth / 2, -tableHeight / 2, ballRadius, paint);
                canvas.drawCircle(+tableWidth / 2, +tableHeight / 2, ballRadius, paint);

                ballRadius = SnookerSize.sidePocketRadius * rate;
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
                    canvas.drawCircle(p.x * rate, p.y * rate, SnookerSize.ballRadius * rate, paint);
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(p.x * rate, p.y * rate, SnookerSize.ballRadius * rate / 2, paint);
                    if (i > 7) {
                        canvas.drawArc(p.x * rate - SnookerSize.ballRadius * rate, p.y * rate - SnookerSize.ballRadius * rate, p.x * rate + SnookerSize.ballRadius * rate, p.y * rate + SnookerSize.ballRadius * rate, -50, 100, false, paint);
                        canvas.drawArc(p.x * rate - SnookerSize.ballRadius * rate, p.y * rate - SnookerSize.ballRadius * rate, p.x * rate + SnookerSize.ballRadius * rate, p.y * rate + SnookerSize.ballRadius * rate, 130, 100, false, paint);
                    }
                    paint.setColor(Color.BLACK);
                    canvas.drawText(String.valueOf(i + 1), p.x * rate, p.y * rate + adjY, paint);
                    if ((isInCornerPocket(p) || isInSidePocket(p)) && isLegalHit) {
                        world.destroyBody(balls[i]);
                        ((UserData) balls[i].getUserData()).isDrawing = false;
                        fellBalls.add(i);
                        if (i == 8) {
                            runOnUiThread(() -> {
                                view.gameOver();
                                if (button.getVisibility() != View.VISIBLE)
                                    button.setVisibility(View.VISIBLE);
                                button.setText(view.isIs1P() ? "1P WIN!" : "2P WIN!");
                                button.setOnClickListener(v -> {
                                    view.gamePrepare();
                                    button.setText("START");
                                    button.setOnClickListener(btn -> {
                                        view.gameStart();
                                        button.setVisibility(View.GONE);
                                    });
                                    restartGame();
                                });
                            });
                        }
                        defineMinBall();
                        view.invalidate();
                    }
                }
                if (((UserData) ball.getUserData()).isDrawing) {
                    p = ball.getPosition();
                    paint.setColor(Color.WHITE);
                    canvas.drawCircle(p.x * rate, p.y * rate, SnookerSize.ballRadius * rate, paint);
                    if (ball.getLinearVelocity().length() > 0.000001)
                        isMoving = true;
                    if ((isInCornerPocket(p) || isInSidePocket(p)) && isLegalHit) {
                        world.destroyBody(ball);
                        ((UserData) ball.getUserData()).isDrawing = false;
                        runOnUiThread(MainActivity.this::freeMode);
                    }
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
                            canvas.drawRect(-(p.x + SnookerSize.horizontalWallWidth / 2) * rate, p.y * rate - 5, (p.x + SnookerSize.horizontalWallWidth / 2) * rate, p.y * rate + 5, paint);
                            break;
                        case 1:
                        case 2:
                        case 4:
                        case 5:
                            canvas.drawRect(p.x * rate - 5, (p.y - SnookerSize.verticalWallHeight / 2) * rate, p.x * rate + 5, (p.y + SnookerSize.verticalWallHeight / 2) * rate, paint);
                            break;
                    }
                }
            }

            {
                //debug
                Body body = world.getBodyList();
                for (int i = 0; i < world.getBodyCount(); i++) {
                    p = body.getPosition();
                    paint.setColor(Color.GRAY);
                    canvas.drawCircle(p.x * rate, p.y * rate, 10, paint);
                    body = body.getNext();
                }
            }

            if (isMoving) {
                if (animator.isRunning())
                    runOnUiThread(() -> animator.cancel());
            } else if (!animator.isRunning() && !view.isCanHitBall()) {
                runOnUiThread(() -> animator.cancel());
                runOnUiThread(() -> animator.start());
            }
        }
    }
}
