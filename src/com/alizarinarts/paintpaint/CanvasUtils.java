package com.alizarinarts.paintpaint;

import static android.opengl.GLES20.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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

}
