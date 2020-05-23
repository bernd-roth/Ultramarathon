package de.tadris.fitness.dto;

public class DataPointDistance {
    float time;
    int distance;

    public DataPointDistance(float x, int y) {
        this.time = x;
        this.distance = y;
    }

    public float getTime() {
        return time;
    }

    public int getDistance() {
        return distance;
    }
}
