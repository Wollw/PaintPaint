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

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import android.os.Environment;

import android.util.Log;

/**
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 * @version 1.0
 * 
 */
public class Canvas {

    CanvasGLSurfaceView mSurfaceView;
    CanvasRenderer mRenderer;

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

    public GLSurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    /**
     * Capture the canvas as a Bitmap.
     */
    public Bitmap getCanvasBitmap() {
        return mRenderer.getCanvasBitmap();
    }

    /**
     * Capture the canvas as a PNG.
     */
    public void saveCanvasPNG(String saveDir, String fileName) {
        if (mRenderer == null) {
            Log.d("", "Renderer is null!");
            return;
        }
        Bitmap b = mRenderer.getCanvasBitmap();
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
}
