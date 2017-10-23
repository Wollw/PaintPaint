package com.alizarinarts.paintpaint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ActivityManager;

import android.content.Context;

import android.content.pm.ConfigurationInfo;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import android.util.Log;

/**
 * This class represents the drawing aspects of the CanvasActivity and serves to
 * keep the layout of the class and menus separate from the drawing function.
 *
 * Note: The entire Canvas design needs rethinking.  Is this class even
 * needed? Too much abstraction?
 * 
 * @author <a href="mailto:rose.e.shere@gmail.com">Rose Shere</a>
 */
public class Canvas {

    CanvasGLSurfaceView mSurfaceView;
    CanvasRenderer mRenderer;

    /**
     * This constructor attempts to setup an OpenGL ES 2.0 SurfaceView and
     * Renderer.  If it fails the program quits.  That should never happen
     * as the minimum API level includes OpenGL ES 2.0.
     */
    public Canvas(Context context) {
        Activity a = (Activity) context;
        ActivityManager am = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        if (info.reqGlEsVersion >= 0x20000) {
            mSurfaceView = new CanvasGLSurfaceView(a);
            a.setContentView(mSurfaceView);
            mRenderer = mSurfaceView.getRenderer();
        } else {
            // No OpenGL ES 2.0
            a.finish();
        }
    }

    public CanvasGLSurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public CanvasRenderer getRenderer() {
        return mRenderer;
    }

    public CanvasBrush getBrush() {
        return mRenderer.getBrush();
    }

    /**
     * Capture the canvas as a Bitmap.
     */
    public Bitmap getCanvasBitmap() {
        return mRenderer.getCanvasBitmap();
    }

    /**
     * Save the canvas to storage as an image.  For now it only saves as PNG.
     *
     * @param saveDir The directory to save the file in.
     * @param fileName The name to save the canvas as.
     */
    public void save(String saveDir, String fileName) {
        /* Make sure we make a valid Bitmap */
        if (mRenderer == null) {
            Log.e(PaintPaint.NAME, "Renderer is null!");
            return;
        }
        Bitmap b = mRenderer.getCanvasBitmap();
        if (b == null) {
            Log.e(PaintPaint.NAME, "Couldn't get bitmap to save!");
            return; // return early if we didn't get a bitmap
        }

        /* Attempt to write the Bitmap to storage */
        try {
            File dir = new File(saveDir+"/");
            dir.mkdirs();
            File outFile = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            b.compress(CompressFormat.PNG, 100, fos);
            try {
                fos.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the canvas to the background color.
     */
    public void clear() {
        mRenderer.clear();
    }
}
