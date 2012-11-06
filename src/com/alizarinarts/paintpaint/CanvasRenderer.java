package com.alizarinarts.paintpaint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import android.graphics.Bitmap;

import static android.opengl.GLES20.*;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import android.util.Log;

/**
 * The OpenGL renderer responsible for creating and maintaining the canvas
 * surface.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 */
public class CanvasRenderer implements GLSurfaceView.Renderer {

    private Resources resources;
    private AssetManager assets;

    private final float[] mVerticesData = {
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 0.0f };

    private int programId;

    // Uniforms for GLSL programs.
    private int mProjectionMatrixHandle;
    private float[] mProjectionMatrix = new float[16];

    // Zoom level
    private int mZoomHandle;
    private float mZoom = 1f;

    // Canvas size
    private int width;
    private int height;

    // OpenGL buffer identifiers
    private int[] buffers = new int[1];

    //private int mTextureId;

    public CanvasRenderer(Context context) {
        resources = context.getResources();
        assets = resources.getAssets();
    }

    /**
     * Generate a shader from a source file.  Returns the shader's identifier.
     *
     * @param fileName The path to the fragment or vertex shader to compile.
     */
    private int makeShader(String fileName) {
        // Read the shader source code
        String buffer = "";
        try {
            InputStream is = assets.open("shaders/" + fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;

            while ((line = br.readLine()) != null) {
                buffer += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Build the shader.
        int shaderType = 0;
        if (fileName.endsWith(".vert")) {
            shaderType = GL_VERTEX_SHADER;
        } else if (fileName.endsWith(".frag")) {
            shaderType = GL_FRAGMENT_SHADER;
        }
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, buffer);
        glCompileShader(shader);
        return shader;
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        /* Create the shader program. This shader program code should probably
         * be abstracted into its own object. */
        programId = glCreateProgram();

        /* Bind the shaders to the program */
        int vs = makeShader("shader.vert");
        int fs = makeShader("shader.frag");
        glAttachShader(programId, vs);
        glAttachShader(programId, fs);

        /* Bind attributes to the program */
        glBindAttribLocation(programId, 0, "vPosition");

        /* Link the program and handle any errors */
        int linkStatus[] = new int[1];
        glLinkProgram(programId);
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e("", "ERROR LINKING");
            Log.e("", glGetProgramInfoLog(programId));
            glDeleteProgram(programId);
            return;
        }

        /* If all went well, use the program. */
        glUseProgram(programId);

        /* Create canvas Vertex Buffer Object */
        FloatBuffer mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertices.put(mVerticesData).position(0);
        glGenBuffers(1, buffers, 0);
        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glBufferData(GL_ARRAY_BUFFER, mVertices.capacity() * 4, mVertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        glEnable(GL_CULL_FACE);

    }

    public void onDrawFrame(GL10 glUnused) {
        // Update graphics, bind resources, and render (steps 6,7)
        glClear(GL_COLOR_BUFFER_BIT);


        // Camera Matrix Setup
        mProjectionMatrixHandle = glGetUniformLocation(programId, "uProjMatrix");
        glUniformMatrix4fv(mProjectionMatrixHandle, 1, false,
                mProjectionMatrix, 0);

        mZoomHandle = glGetUniformLocation(programId, "uZoom");
        glUniform1f(mZoomHandle, mZoom);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        /* Texture related stuff I don't have working yet. */
        //glActiveTexture(GL_TEXTURE0);
        //glBindTexture(GL_TEXTURE_2D, mTextureId);
        //glUniform1i(glGetUniformLocation(program, "uTexture"), 0);
        //glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        //glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        //glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        //glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Adjust camera/rendering parameters
        glViewport(0, 0, width, height);

        /* These lines would create an aspect ratio correct square. */
        //float aspectRatio = (float) height / width;
        //Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1-aspectRatio, 1+aspectRatio, -1, 1);
        //Matrix.orthoM(mProjectionMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1f, 1f);

        Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1, 1);

        this.width = width;
        this.height = height;

        /* Load Texture */
        //mTextureId = loadTexture();
    }

    /*
     * Create an OpenGL texture and load it onto the GPU
     * Just for testing purposes for now. Returns the texture's identifier.
     */
    public int loadTexture() {

        int[] textureId = new int[1];
        ByteBuffer bb = ByteBuffer.allocateDirect(width*height*4);
        IntBuffer  ib = bb.asIntBuffer();

        /* Create a checkboard texture */
        for (int i = 0; i < width*height; i++) {
            if (i % 2 == 0)
                ib.put(0xffffffff); // Black
            else 
                ib.put(0x000000ff); // White
        }

        // Create and bind a single texture object.
        glGenTextures(1, textureId, 0);
        glBindTexture(GL_TEXTURE_2D, textureId[0]);

        // Copy the texture to the GPU
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);

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
     * Change the texture at x, y
     *
     * based on code from http://stackoverflow.com/questions/1152683/how-to-change-single-texel-in-opengl-texture
     *
     * @param x The X coordinate
     * @param y The y coordinate
     */
    public void editTexture(int x, int y) {
        /*
        ib.put(0x00FFFFFF);
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        glTexSubImage2D(GL_TEXTURE_2D, 0,
                x, y,
                1, 1, // width and height
                GL_RGBA, GL_UNSIGNED_BYTE,
                bb);*/

    }

}
