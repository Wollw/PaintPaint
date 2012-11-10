package com.alizarinarts.paintpaint;

/**
 * Represents a single paint dab on the canvas.  Used for queueing draw events.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 * @version 1.0
 */
public class CanvasDab {
    private final float x;
    private final float y;
    private final float pressure;

    public CanvasDab(float x, float y, float pressure) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
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

}
