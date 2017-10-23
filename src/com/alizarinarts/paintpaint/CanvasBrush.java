package com.alizarinarts.paintpaint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import java.util.Queue;

import android.graphics.Bitmap;

import static android.opengl.GLES20.*;

import android.util.Log;

/**
 * This class maintains the state and methods used with the paint brush.
 * These include the methods used for drawing brush strokes as well as all the
 * size and color information associated with the brush.
 *
 * @author <a href="mailto:rose.e.shere@gmail.com">Rose Shere</a>
 * @version 1.0
 */
public class CanvasBrush {

    private static final int BRUSH_PIXEL_SIZE = 32;

    /* OpenGL identifiers */
    private int shaderProgramId;
    private int textureId;
    private int textureCoordBufferId;
    private int vertexBufferId;
    private int maskId;

    /* Default size and color values for the brush */
    private float size = 1.0f;
    private int color = 0x000000ff;

    /* Vertex information used to draw the brush dabs' polygons */
    private final float[] vertexData = {
        -.05f, -.05f,  0.0f,
         .05f, -.05f,  0.0f,
        -.05f,  .05f,  0.0f,
         .05f,  .05f,  0.0f,
    };

    /* Coordinates for texture mapping the brush texture to the polygon */
    private final float[] textureCoordData = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    };

    /* Position of the last brush dab */
    private float lastX;
    private float lastY;

    /* The number of dabs to draw between actual touch events.
     * The higher this is the more filled in a brush stroke will look. */
    private int dabSteps = 45;

    public CanvasBrush(int shaderProgramId) {

        this.shaderProgramId = shaderProgramId;

        /* Create the brush texture */
        textureId = CanvasUtils.makeTexture(32, 32, color);

        /* Send the brush vertices to the GPU */
        FloatBuffer vertices = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertices.put(vertexData).position(0);
        int[] buffer = new int[1];
        glGenBuffers(1, buffer, 0);
        vertexBufferId = buffer[0];
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices.capacity() * 4, vertices, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        /* Send Texture Coordinate data to the GPU */
        FloatBuffer textureCoords = ByteBuffer.allocateDirect(textureCoordData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureCoords.put(textureCoordData).position(0);
        glGenBuffers(1, buffer, 0);
        textureCoordBufferId = buffer[0];
        glBindBuffer(GL_ARRAY_BUFFER, textureCoordBufferId);
        glBufferData(GL_ARRAY_BUFFER, textureCoords.capacity() * 4, textureCoords, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }

    /**
     * Draw a queue of brush dabs to the canvas and their interpolated
     * connecting dabs.
     *
     * @param dabs a queue of paint brush dabs
     */
    public void drawQueue(Queue<CanvasDab> dabs) {
        glUseProgram(shaderProgramId);

        int aTexCoord = glGetAttribLocation(shaderProgramId, "aTextureCoord");

        // Enable the textures
        // Multiple texture code example from
        // http://opengles2learning.blogspot.com/2011/06/multi-texturing.html
        /* Texture 0 is the brush's color */
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(glGetUniformLocation(shaderProgramId, "uTexture"), 0);

        /* Texture 1 is the brush's alpha map. Effectively the brush's shape */
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, maskId);
        glUniform1i(glGetUniformLocation(shaderProgramId, "uMask"), 1);

        // Enable the vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        int aVertPos = glGetAttribLocation(shaderProgramId, "aVertexPosition");
        glEnableVertexAttribArray(aVertPos);
        glVertexAttribPointer(aVertPos, 3, GL_FLOAT, false, 0, 0);

        // Enable the texture coordinates
        glBindBuffer(GL_ARRAY_BUFFER, textureCoordBufferId);
        glEnableVertexAttribArray(aTexCoord);
        glVertexAttribPointer(aTexCoord, 2, GL_FLOAT, false, 0, 0);

        /* Draw the brush dabs */
        while (!dabs.isEmpty()) {
            CanvasDab dab = dabs.poll();
            float x, y;

            if (dab.isNewStroke()) {
                /* A new stroke starts at the dab's current position...*/
                Log.d(PaintPaint.NAME, "new stroke");
                x = dab.getX();
                y = dab.getY();
            } else {
                /* ...but if it isn't a new stroke it must start at the last
                 * position in order to fill in the space between dabs. */
                x = lastX;
                y = lastY;
            }

            /* Draw interpolated steps from the last dab to the current dab */
            int i = dabSteps;
            while ( i-- != 0 ) {

                // Set the brush dab location
                glUniform2f(glGetUniformLocation(shaderProgramId, "uOffset"), x, y);
                glUniform1f(glGetUniformLocation(shaderProgramId, "uZoom"), dab.getPressure()*2*size);

                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

                if (dab.isNewStroke()){
                    break;
                } else {
                    /* calculate the next brush mark position to paint */
                    float dx = (dab.getX() - lastX);
                    float dy = (dab.getY() - lastY);
                    x += dx / dabSteps;
                    y += dy / dabSteps;
                }

            }
            /* Update the last brush position to the newly completed dab */
            lastX = dab.getX();
            lastY = dab.getY();
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public float getSize() {
        return size * 50f;
    }

    public void setSize(float size) {
        this.size = size / 50f;
    }

    public void setDabSteps(int steps) {
        this.dabSteps = steps;
    }

    public int getDabSteps() {
        return dabSteps;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        glDeleteTextures(1, new int[] { textureId }, 0);
        textureId = CanvasUtils.makeTexture(BRUSH_PIXEL_SIZE, BRUSH_PIXEL_SIZE, color);
        this.color = color;
    }

    public void setMask(Bitmap b) {
        glDeleteTextures(1, new int[] { maskId }, 0);
        maskId = CanvasUtils.makeTexture(b, b.getWidth(), b.getHeight());
    }
}
