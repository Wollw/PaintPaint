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

public class CanvasRenderer implements GLSurfaceView.Renderer {

    private Resources resources;
    private AssetManager assets;

    //private FloatBuffer mVertices;
    private final float[] mVerticesData = {
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 0.0f };

    private int program;

    /**
     * Uniforms for GLSL programs.
     */
    private int mProjectionMatrixHandle;
    private float[] mProjectionMatrix = new float[16];

    private int mZoomHandle;
    private float mZoom = 1f;

    private int width;
    private int height;

    private int[] buffers = new int[1];

    //private int mTextureId;

    public CanvasRenderer(Context context) {
        resources = context.getResources();
        assets = resources.getAssets();
    }

    /**
     * Generate a shader from a source file.  Returns the shader's identifier.
     *
     * @param filePath The path to the fragment or vertex shader to compile.
     */
    private int makeShader(String filePath) {
        // Read the shader source code
        String buffer = "";
        try {
            InputStream is = assets.open("shaders/" + filePath);
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
        if (filePath.endsWith(".vert")) {
            shaderType = GL_VERTEX_SHADER;
        } else if (filePath.endsWith(".frag")) {
            shaderType = GL_FRAGMENT_SHADER;
        }
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, buffer);
        glCompileShader(shader);
        return shader;
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // Initialization (steps 3,4,5)

        // Step 3a: Create, load, and compile the vertex and fragment shaders.
        int vs = makeShader("shader.vert");
        int fs = makeShader("shader.frag");

        // Step 3b: Create the shader program.
        program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);

        // Bind vPosition to attribute 1
        glBindAttribLocation(program, 0, "vPosition");

        // Step 3c: Link the program and check for errors.
        int results[] = new int[1];
        glLinkProgram(program);
        glGetProgramiv(program, GL_LINK_STATUS, results, 0);
        if (results[0] == 0) {
            Log.e("MADDER", "ERROR LINKING");
            Log.e("MADDER", glGetProgramInfoLog(program));
            glDeleteProgram(program);
            return;
        }

        /* Create VBO */
        FloatBuffer mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
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

        glUseProgram(program);

        // Camera Matrix Setup
        mProjectionMatrixHandle = glGetUniformLocation(program, "uProjMatrix");
        glUniformMatrix4fv(mProjectionMatrixHandle, 1, false,
                mProjectionMatrix, 0);

        mZoomHandle = glGetUniformLocation(program, "uZoom");
        glUniform1f(mZoomHandle, mZoom);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        //glActiveTexture(GL_TEXTURE0);
        //glBindTexture(GL_TEXTURE_2D, mTextureId);
        //glUniform1i(glGetUniformLocation(program, "uTexture"), 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Adjust camera/rendering parameters
        glViewport(0, 0, width, height);

        //float aspectRatio = (float) height / width;
        //Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1-aspectRatio, 1+aspectRatio, -1, 1);
        //Matrix.orthoM(mProjectionMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1f, 1f);
        Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1, 1);

        this.width = width;
        this.height = height;

        /* Load Texture */
        //mTextureId = loadTexture();
    }

    public int loadTexture() {
        int[] textureId = new int[1];

        //int width = 1024;
        //int height = 1024;
        ByteBuffer bb = ByteBuffer.allocateDirect(width*height*4);
        IntBuffer  ib = bb.asIntBuffer();

        Log.d("", "Canvas Size: "+width+"x"+height);

        for (int i = 0; i < width*height; i++) {
            if (i % 2 == 0)
                ib.put(0xff0000ff);
            else 
                ib.put(0x000000ff);
        }

        glGenTextures(1, textureId, 0);
        glBindTexture(GL_TEXTURE_2D, textureId[0]);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);

        glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );

        return textureId[0];

    }

    /**
     * Capture the canvas as a Bitmap.
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
     * Change the texture at x, y;
     *
     * based on code from http://stackoverflow.com/questions/1152683/how-to-change-single-texel-in-opengl-texture
     */
    public void editTexture(int x, int y) {
        ByteBuffer bb = ByteBuffer.allocateDirect(4);
        IntBuffer  ib = bb.asIntBuffer();
        glReadPixels(x,y,1,1, GL_RGBA, GL_UNSIGNED_BYTE, ib);
        Log.d("",""+bb.get(0)+","+bb.get(1)+","+bb.get(2)+","+bb.get(3));
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
