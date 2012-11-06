package com.alizarinarts.paintpaint;

import android.content.Context;

import android.opengl.GLSurfaceView;

import android.view.MotionEvent;

/**
 * Custom GLSurfaceView to allow touch events.  Based on example from:
 * http://android-developers.blogspot.com/2009/04/introducing-glsurfaceview.html
 * 
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 */
public class CanvasGLSurfaceView extends GLSurfaceView {

    CanvasRenderer mRenderer;

    public CanvasGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        mRenderer = new CanvasRenderer(context);
        setRenderer(mRenderer);
    }

    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Add Runnable to queue, idea from https://github.com/MasDennis/Rajawali/issues/56
                queueEvent(new Runnable() {public void run() {
                    mRenderer.editTexture((int)event.getX(), (int)event.getY());
                }});
                return true;
        }

        return false;
    }

    public CanvasRenderer getRenderer() {
        return mRenderer;
    }

}
