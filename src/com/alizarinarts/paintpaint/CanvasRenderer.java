package com.alizarinarts.paintpaint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import android.graphics.Bitmap;

import static android.opengl.GLES20.*;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

/**
 * The OpenGL renderer responsible for creating and maintaining the canvas
 * surface.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 */
public class CanvasRenderer implements GLSurfaceView.Renderer {

    private Resources resources;
    private AssetManager assets;

    private final float[] verticesData = {
        -.05f, -.05f,  0.0f,
         .05f, -.05f,  0.0f,
        -.05f,  .05f,  0.0f,
         .05f,  .05f,  0.0f,
    };

    private final float[] textureCoordData = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    };

    CanvasShaderProgram canvasShaderProgram;
    private int programId;
    private int textureId;

    // Uniforms for GLSL programs.
    private int projectionMatrixHandle;
    private float[] projectionMatrix = new float[16];

    // Zoom level
    private int zoomHandle;
    private float zoom = 1f;

    // Canvas size
    private int width;
    private int height;

    // Shader Attributes
    private int aVertexPosition;
    private int aTextureCoord;

    private int uOffset;

    // OpenGL buffer identifiers
    private int verticesBuffer;
    private int textureCoordBuffer;

    // The draw queue for brush events.
    Queue<CanvasDab> drawQueue = new LinkedList<CanvasDab>();

    public CanvasRenderer(Context context) {
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

        /* Send the canvas vertices to the GPU */
        FloatBuffer vertices = ByteBuffer.allocateDirect(verticesData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertices.put(verticesData).position(0);
        int[] buffer = new int[1];
        glGenBuffers(1, buffer, 0);
        verticesBuffer = buffer[0];
        glBindBuffer(GL_ARRAY_BUFFER, verticesBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertices.capacity() * 4, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        /* Send Texture Coordinate data to the GPU */
        FloatBuffer textureCoords = ByteBuffer.allocateDirect(textureCoordData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureCoords.put(textureCoordData).position(0);
        glGenBuffers(1, buffer, 0);
        textureCoordBuffer = buffer[0];
        glBindBuffer(GL_ARRAY_BUFFER, textureCoordBuffer);
        glBufferData(GL_ARRAY_BUFFER, textureCoords.capacity() * 4, textureCoords, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        /* Load Texture */
        textureId = loadTexture();

        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glEnable(GL_CULL_FACE);

    }

    public void onDrawFrame(GL10 glUnused) {
        // Update graphics, bind resources, and render (steps 6,7)
        //glClear(GL_COLOR_BUFFER_BIT);

        // Camera Matrix Setup
        projectionMatrixHandle = glGetUniformLocation(programId, "uProjMatrix");
        glUniformMatrix4fv(projectionMatrixHandle, 1, false,
                projectionMatrix, 0);
        zoomHandle = glGetUniformLocation(programId, "uZoom");
        glUniform1f(zoomHandle, zoom);

        // Enable the texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(glGetUniformLocation(programId, "uTexture"), 0);

        // Enable the vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, verticesBuffer);
        glEnableVertexAttribArray(aVertexPosition);
        glVertexAttribPointer(aVertexPosition, 3, GL_FLOAT, false, 0, 0);

            glUniform2f(glGetUniformLocation(programId, "uOffset"), 0, 0);

            // Enable the texture
            glBindBuffer(GL_ARRAY_BUFFER, textureCoordBuffer);
            glEnableVertexAttribArray(aTextureCoord);
            glVertexAttribPointer(aTextureCoord, 2, GL_FLOAT, false, 0, 0);

            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        while (!drawQueue.isEmpty()) {
            CanvasDab dab = drawQueue.poll();
            // Set the offset
            glUniform2f(glGetUniformLocation(programId, "uOffset"), dab.getX(), dab.getY());

            // Enable the texture
            glBindBuffer(GL_ARRAY_BUFFER, textureCoordBuffer);
            glEnableVertexAttribArray(aTextureCoord);
            glVertexAttribPointer(aTextureCoord, 2, GL_FLOAT, false, 0, 0);

            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        }

    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Adjust camera/rendering parameters
        glViewport(0, 0, width, height);

        /* These lines would create an aspect ratio correct square. */
        float aspectRatio = (float) height / width;
        //Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1-aspectRatio, 1+aspectRatio, -1, 1);
        Matrix.orthoM(projectionMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1f, 1f);

        //Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -1f, 1f, -1, 1);

        this.width = width;
        this.height = height;


    }

    /*
     * Create an OpenGL texture and load it onto the GPU
     * Just for testing purposes for now. Returns the texture's identifier.
     */
    public int loadTexture() {
        int size_x = 32;
        int size_y = 32;
        int[] textureId = new int[1];
        ByteBuffer bb = ByteBuffer.allocateDirect(size_x*size_y*4);
        IntBuffer  ib = bb.asIntBuffer();

        //int color = 0x0000ffff;
        for (int i = 0; i < size_x * size_y; i++) {
            ib.put(0x000000ff);
        }

        // Create and bind a single texture object.
        glGenTextures(1, textureId, 0);
        glBindTexture(GL_TEXTURE_2D, textureId[0]);

        // Copy the texture to the GPU
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size_x, size_y, 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);
        
        glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );

        glBindTexture(GL_TEXTURE_2D, 0);

        return textureId[0];
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
        Bitmap sb = Bitmap.createBitmap(bt, width, height, Bitmap.Config.RGB_565);
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

        drawQueue.offer(new CanvasDab(offsetX, offsetY, p));
    }

}
