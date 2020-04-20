package de.tadris.fitness.dto;

public class DataPointAverageSpeed {
    float time;
    float averageSpeed;

    public DataPointAverageSpeed(float x, float y) {
        this.time = x;
        this.averageSpeed = y;
    }

    public float getTime() {
        return time;
    }

    public float getAverageSpeed() {
        return averageSpeed;
    }
}
