package com.alizarinarts.paintpaint;

import android.graphics.Bitmap;

import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.opengl.GLUtils;

import android.util.Log;

/**
 * Commonly used utilities for rendering and maintaining the canvas.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 * @version 1.0
 */
public class CanvasUtils {

    /*
     * Create an OpenGL texture and load it onto the GPU
     * Just for testing purposes for now. Returns the texture's identifier.
     *
     * x and y must be powers of 2
     *
     * @param x the width of the texture
     * @param y the height of the texture
     * @param color the color value
     */
    public static int makeTexture(int x, int y, int color) {
        int[] tid = new int[1];
        ByteBuffer bb = ByteBuffer.allocateDirect(x*y*4);
        IntBuffer  ib = bb.asIntBuffer();

        //int color = 0x0000ffff;
        for (int i = 0; i < x * y; i++) {
            ib.put(color);
        }

        // Create and bind a single texture object.
        glGenTextures(1, tid, 0);
        glBindTexture(GL_TEXTURE_2D, tid[0]);

        // Copy the texture to the GPU
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, x, y, 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Unbind texture
        glBindTexture(GL_TEXTURE_2D, 0);

        return tid[0];
    }

    /* Make a texture from a bitmap or a new blank canvas if Bitmap is null
     *
     * @param bitmap the bitmap to use on the texture
     */
    public static int makeTexture(Bitmap bitmap) {
        int textureId = makeTexture(PaintPaint.TEXTURE_SIZE, PaintPaint.TEXTURE_SIZE, 0xffffffff);
        if (bitmap != null) {
            Bitmap flippedbmp = flipBitmap(bitmap);
            glBindTexture(GL_TEXTURE_2D, textureId);
            GLUtils.texSubImage2D(GL_TEXTURE_2D, 0, 0, 0, flippedbmp);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        return textureId;
    }

    /** Flips the pixels in a Bitmap to be mirrored vertically.  This is needed
     * for OpenGL to draw the texture properly as loads texture bottom up.
     * @return a new Bitmap that is identical to the argument but flipped.
     * @param bitmap The Bitmap to flip the pixels of
     */
    static public Bitmap flipBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pixels = new int[w*h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            pixels[y*w+x] = bitmap.getPixel(x, h-1-y);
        }}
        bitmap = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

}
