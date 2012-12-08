package com.alizarinarts.paintpaint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import android.content.res.AssetManager;

import static android.opengl.GLES20.*;

import android.util.Log;

/**
 * A class used to group the GLSL shaders and compiled program together.
 * Handles the compilation of shaders into a program.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 */
public class CanvasShaderProgram {

    private AssetManager assets;

    private int program;
    private int vertexShader;
    private int fragmentShader;

    public CanvasShaderProgram(String vertexSrc, String fragmentSrc, AssetManager assets) {
        this.assets = assets;

        program = glCreateProgram();

        /* Bind the shaders to the program */
        int vs = makeShader(vertexSrc);
        int fs = makeShader(fragmentSrc);
        glAttachShader(program, vs);
        glAttachShader(program, fs);

        /* Link the program and handle any errors */
        int linkStatus[] = new int[1];
        glLinkProgram(program);
        glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e(PaintPaint.NAME, "ERROR LINKING");
            Log.e(PaintPaint.NAME, glGetProgramInfoLog(program));
            glDeleteProgram(program);
            return;
        }
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

    int getProgram() {
        return program;
    }

    int getVertexShader() {
        return vertexShader;
    }

    int getFragmentShader() {
        return fragmentShader;
    }

}
