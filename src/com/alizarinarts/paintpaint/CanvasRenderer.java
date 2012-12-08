package com.alizarinarts.paintpaint;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import static android.opengl.GLES20.*;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import android.util.Log;

/**
 * The OpenGL renderer responsible for creating and maintaining the canvas
 * surface.  This is where the drawing happens.
 *
 * The main idea behind this is that CanvasDab objects are added to a Queue and
 * then drawn to an invisible framebuffer texture.  This texture is then drawn
 * to a polygon on the screen framebuffer for display.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 */
public class CanvasRenderer implements GLSurfaceView.Renderer {

    private Resources resources;
    private AssetManager assets;
    private SharedPreferences settings;

    private Bitmap restoreBitmap = null;

    // The default value for the canvas texture dimensions
    private int textureSize = 2048;

    private final float[] canvasVerticesData = {
        -1.0f, -1.0f,  0.0f,
         1.0f, -1.0f,  0.0f,
        -1.0f,  1.0f,  0.0f,
         1.0f,  1.0f,  0.0f,
    };

    private final float[] textureCoordData = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    };

    CanvasShaderProgram canvasShaderProgram;
    private int programId;

    private int canvasTextureId;
    private int canvasMaskId;

    // Uniforms for GLSL programs.
    private int projectionMatrixHandle;
    private float[] projectionMatrix = new float[16];

    // Zoom level
    private int zoomHandle;
    private float canvasZoom = 1.0f;

    // Canvas size
    private int width;
    private int height;

    // Shader Attributes
    private int aVertexPosition;
    private int aTextureCoord;

    //private int uOffset;

    // OpenGL buffer identifiers
    private int canvasVerticesBuffer;
    private int textureCoordBuffer;

    private int framebuffer;

    // The brush object used for drawing
    CanvasBrush brush;

    // The draw queue for brush events.
    Queue<CanvasDab> drawQueue = new LinkedList<CanvasDab>();

    // Flag used to indicate if canvas should be cleared.
    boolean willClear = false;

    // Flag to tell if we can should allow the canvas to be autosaved yet
    // Set to true on the first instance of a brush stroke being drawn.
    boolean canAutosave = false;

    public CanvasRenderer(Context context) {
        settings = ((Activity)context).getPreferences(0);
        resources = context.getResources();
        assets = resources.getAssets();
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        // Create the shader program
        canvasShaderProgram = new CanvasShaderProgram("shader.vert", "shader.frag", assets);
        programId = canvasShaderProgram.getProgram();

        glUseProgram(programId);

        aVertexPosition = glGetAttribLocation(programId, "aVertexPosition");
        glEnableVertexAttribArray(aVertexPosition);

        aTextureCoord = glGetAttribLocation(programId, "aTextureCoord");
        glEnableVertexAttribArray(aTextureCoord);

        /* Send Texture Coordinate data to the GPU */
        int[] buffer = new int[1];
        FloatBuffer textureCoords = ByteBuffer.allocateDirect(textureCoordData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureCoords.put(textureCoordData).position(0);
        glGenBuffers(1, buffer, 0);
        textureCoordBuffer = buffer[0];
        glBindBuffer(GL_ARRAY_BUFFER, textureCoordBuffer);
        glBufferData(GL_ARRAY_BUFFER, textureCoords.capacity() * 4, textureCoords, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        /* Create the brush */
        brush = new CanvasBrush(programId);
        try {
            brush.setMask(BitmapFactory.decodeStream(assets.open("brushes/round.png")));
        } catch (IOException e) {
            Log.e(PaintPaint.NAME, "Failed to load brush mask.");
        }
        brush.setSize(settings.getFloat("BRUSH_SIZE", 1.0f));
        brush.setColor(settings.getInt("BRUSH_COLOR", 0x000000ff));
        brush.setDabSteps(settings.getInt("BRUSH_DABS", 45));

        glEnable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);


    }

    public void onDrawFrame(GL10 glUnused) {

        glClear(GL_COLOR_BUFFER_BIT);

        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

        if (willClear) {
            Log.d(PaintPaint.NAME, "Clearing Canvas");
            glClear(GL_COLOR_BUFFER_BIT);
            willClear = false;
        }

        // Setup the projection matrix and get the zoom level uniform identifier
        projectionMatrixHandle = glGetUniformLocation(programId, "uProjMatrix");
        glUniformMatrix4fv(projectionMatrixHandle, 1, false,
                projectionMatrix, 0);
        zoomHandle = glGetUniformLocation(programId, "uZoom");

        // Allow autosaving when something has been drawn to the canvas.
        if (!canAutosave && drawQueue.size() > 0)
            canAutosave = true;

        /* This is where all the brush marks are drawn to the framebuffer */
        brush.drawQueue(drawQueue);

        glUseProgram(programId);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        /* Draw the framebuffer to the screen */

        // Set canvas zoom level
        glUniform1f(zoomHandle, canvasZoom);
        // Enable the texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, canvasTextureId);
        glUniform1i(glGetUniformLocation(programId, "uTexture"), 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, canvasMaskId);
        glUniform1i(glGetUniformLocation(programId, "uMask"), 1);

        // Enable the vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, canvasVerticesBuffer);
        glEnableVertexAttribArray(aVertexPosition);
        glVertexAttribPointer(aVertexPosition, 3, GL_FLOAT, false, 0, 0);

        // Set the offset
        glUniform2f(glGetUniformLocation(programId, "uOffset"), 0, 0);

        // Enable the texture
        glBindBuffer(GL_ARRAY_BUFFER, textureCoordBuffer);
        glEnableVertexAttribArray(aTextureCoord);
        glVertexAttribPointer(aTextureCoord, 2, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        /* Unbind everything */
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Adjust camera/rendering parameters
        glViewport(0, 0, width, height);

        float aspectRatio = (float) height / width;
        Matrix.orthoM(projectionMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1f, 1f);

        /* Load Textures */
        if (width <= textureSize / 2 && height <= textureSize / 2)
            textureSize /= 2; // Half textureSize if we can to save memory
        canvasTextureId = CanvasUtils.makeTexture(restoreBitmap, textureSize, textureSize);
        if (restoreBitmap != null) {
            restoreBitmap = null;
        }
        canvasMaskId = CanvasUtils.makeTexture(null, 8, 8);

        /* Send the canvas polygon's vertices to the GPU */
        FloatBuffer vertices = ByteBuffer.allocateDirect(canvasVerticesData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        float l = 2f*textureSize/width;
        float h = 2f*textureSize*aspectRatio/height;
        vertices.put(new float[]{
                -1.0f,     -aspectRatio,   0f,
                l - 1,     -aspectRatio,   0f,
                -1.0f,  h - aspectRatio,   0f,
                l - 1,  h - aspectRatio,   0f})
            .position(0);
        int[] buffer = new int[1];
        glGenBuffers(1, buffer, 0);
        canvasVerticesBuffer = buffer[0];
        glBindBuffer(GL_ARRAY_BUFFER, canvasVerticesBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertices.capacity() * 4, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);


        /* Create framebuffer 
         * Reference: http://www.songho.ca/opengl/gl_fbo.html
         * Reference: http://www.opengl.org/wiki/GLAPI/glFramebufferRenderbuffer
         * */
        glGenFramebuffers(1, buffer, 0);
        framebuffer = buffer[0];
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, canvasTextureId, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        switch(glCheckFramebufferStatus(GL_FRAMEBUFFER)) {
        case GL_FRAMEBUFFER_COMPLETE:
            Log.d(PaintPaint.NAME,"The fbo is complete");
            break;
        case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
            Log.d(PaintPaint.NAME,"GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            break;
        case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
            Log.d(PaintPaint.NAME,"GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            break;   
        case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
            Log.d(PaintPaint.NAME,"GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS");
            break;
        }
        
        this.width = width;
        this.height = height;

    }

    /**
     * Set the canvas' bitmap to some other bitmap.  Used to load files and
     * restore after activity changes.
     *
     * Example code from http://www.learnopengles.com/android-lesson-four-introducing-basic-texturing/
     *
     * @param bitmap The bitmap to load as to the canvas.
     */
    public void setCanvasBitmap(Bitmap bitmap) {
        restoreBitmap = bitmap;
        int newTexture = CanvasUtils.makeTexture(bitmap, textureSize, textureSize);
        if (canvasTextureId != 0)
            glDeleteTextures(1, new int[]{canvasTextureId}, 0);
        canvasTextureId = newTexture;
    }

    /**
     * Return the canvas as a Bitmap.
     * Based on example from:
     * http://www.anddev.org/how_to_get_opengl_screenshot__useful_programing_hint-t829.html
     */
    public Bitmap getCanvasBitmap() {
        int b[] = new int[width*height];
        int bt[] = new int[width*height];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);
        glReadPixels(0,0,width,height, GL_RGBA, GL_UNSIGNED_BYTE, ib);

        /* OpenGL -> Android Bitmap correction */
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pix = b[i * width + j];
                int pb = (pix >> 16) & 0x000000ff;
                int pr = (pix << 16) & 0x00ff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(height - i - 1) * width + j]= pix1;
            }
        }

        Log.d(PaintPaint.NAME, "canAutosave = " + canAutosave);
        Bitmap sb;
        if (canAutosave) {
            sb = Bitmap.createBitmap(bt, width, height, Bitmap.Config.RGB_565);
        } else {
            sb = null;
        }
        return sb;
    }

    /**
     * Add a new brush dab to the draw queue.
     *
     * @param x The X coordinate
     * @param y The y coordinate
     * @param p The pressure of the dab
     */
    public void addCanvasDab(int x, int y, float p, boolean newEvent) {
        float offsetX = ((float)x - (width / 2))/width*2;
        float offsetY = -((float)y - (height / 2))/height*2;

        drawQueue.offer(new CanvasDab(offsetX, offsetY, p, newEvent));
    }

    /* Sets a flag that tells the renderer to clear the canvas to the background
     * color. */
    public void clear() {
        willClear = true;
    }

    public CanvasBrush getBrush() {
        return brush;
    }

}
