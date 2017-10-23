package com.alizarinarts.paintpaint;

/**
 * Represents a single paint dab on the canvas.  Used for queueing draw events.
 *
 * @author <a href="mailto:rose.e.shere@gmail.com">Rose Shere</a>
 * @version 1.0
 */
public class CanvasDab {
    private final float x;
    private final float y;
    private final float pressure;
    private final boolean newStroke;

    public CanvasDab(float x, float y, float pressure, boolean newStroke) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.newStroke = newStroke;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPressure() {
        return pressure;
    }

    public boolean isNewStroke() {
        return newStroke;
    }

}
