package com.alizarinarts.paintpaint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import java.util.Queue;

import static android.opengl.GLES20.*;

import android.util.Log;

/**
 * This class maintains the state and methods used with the paint brush.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 * @version 1.0
 */
public class CanvasBrush {

    private static final int BRUSH_PIXEL_SIZE = 32;

    private int textureId;
    private int textureCoordBufferId;
    private int vertexBufferId;


    private int shaderProgramId;

    private float size = 1.0f;
    private int color = 0x000000ff;

    private final float[] vertexData = {
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
     * Draw a queue of brush dabs to the canvas
     *
     * @param dabs a queue of paint brush dabs
     *
     */
    public void drawQueue(Queue<CanvasDab> dabs) {

        glUniform1f(glGetUniformLocation(shaderProgramId, "uZoom"), size);

        // Enable the texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(glGetUniformLocation(shaderProgramId, "uTexture"), 0);
        int aTexCoord = glGetAttribLocation(shaderProgramId, "aTextureCoord");

        // Enable the vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        int aVertPos = glGetAttribLocation(shaderProgramId, "aVertexPosition");
        glEnableVertexAttribArray(aVertPos);
        glVertexAttribPointer(aVertPos, 3, GL_FLOAT, false, 0, 0);

        while (!dabs.isEmpty()) {
            CanvasDab dab = dabs.poll();
            // Set the offset
            glUniform2f(glGetUniformLocation(shaderProgramId, "uOffset"), dab.getX(), dab.getY());

            // Enable the texture
            glBindBuffer(GL_ARRAY_BUFFER, textureCoordBufferId);
            glEnableVertexAttribArray(aTexCoord);
            glVertexAttribPointer(aTexCoord, 2, GL_FLOAT, false, 0, 0);

            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public float getSize() {
        return size * 50f;
    }

    public void setSize(float size) {
        this.size = size / 50f;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        Log.d(PaintPaint.NAME,""+color);
        glDeleteTextures(1, new int[] { textureId }, 0);
        textureId = CanvasUtils.makeTexture(BRUSH_PIXEL_SIZE, BRUSH_PIXEL_SIZE, color);
        this.color = color;
    }
}
