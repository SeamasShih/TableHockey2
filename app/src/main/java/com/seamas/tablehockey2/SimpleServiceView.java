package com.seamas.tablehockey2;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleServiceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private boolean mIsDrawing;
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private long FPS = 60;
    private CanvasUpdateProcess canvasUpdateProcess;

    public SimpleServiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        setFocusable(true);
        setKeepScreenOn(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        while (mIsDrawing) {
            drawSomething();

            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void drawSomething() {
        try {
            mCanvas = mSurfaceHolder.lockCanvas();

            if (canvasUpdateProcess != null)
                canvasUpdateProcess.onDraw(mCanvas);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    public void setCanvasUpdateProcess(CanvasUpdateProcess canvasUpdateProcess) {
        this.canvasUpdateProcess = canvasUpdateProcess;
    }

    public long getFPS() {
        return FPS;
    }

    public void setFPS(long FPS) {
        this.FPS = FPS;
    }
}
