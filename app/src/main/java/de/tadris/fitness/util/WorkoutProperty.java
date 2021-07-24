package de.tadris.fitness.util;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.R;

public enum WorkoutProperty {
    START(0),
    END(1),
    DURATION(2),
    PAUSE_DURATION(3),
    AVG_HEART_RATE(4),
    MAX_HEART_RATE(5),
    CALORIE(6),

    // GPS Workout
    LENGTH(7),
    AVG_SPEED(8),
    TOP_SPEED(9),
    AVG_PACE(10),
    ASCENT(11),
    DESCENT(12),

    // Indoor Workout
    AVG_FREQUENCY(13),
    MAX_FREQUENCY(14),
    MAX_INTENSITY(15),
    AVG_INTENSITY(16);

    private int id;

    WorkoutProperty(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isBaseProperty() { return id <=CALORIE.getId(); }
    public boolean isGPSProperty() { return CALORIE.getId() < id && id <= DESCENT.getId(); }
    public boolean isIndoorProperty() { return DESCENT.getId() < id && id <=CALORIE.getId(); }

    public String getStringRepresentation(Context ctx) {
        return getStringRepresentations(ctx).get(id);
    }

    public static List<String> getStringRepresentations(Context context) {
        List<String> criteria = new ArrayList<>();
        criteria.add(context.getString(R.string.workoutStartTime));
        criteria.add(context.getString(R.string.workoutEndTime));
        criteria.add(context.getString(R.string.workoutDuration));
        criteria.add(context.getString(R.string.workoutPauseDuration));
        criteria.add(context.getString(R.string.workoutAvgHeartRate));
        criteria.add(context.getString(R.string.workoutMaxHeartRate));
        criteria.add(context.getString(R.string.workoutBurnedEnergy));

        // GPS Workout
        criteria.add(context.getString(R.string.workoutDistance));
        criteria.add(context.getString(R.string.workoutAvgSpeedShort));
        criteria.add(context.getString(R.string.workoutTopSpeed));
        criteria.add(context.getString(R.string.workoutPace));
        criteria.add(context.getString(R.string.workoutAscent));
        criteria.add(context.getString(R.string.workoutDescent));

        // Indoor Workout
        criteria.add(context.getString(R.string.workoutFrequency));
        criteria.add(context.getString(R.string.workoutMaxFrequency));
        criteria.add(context.getString(R.string.workoutIntensity));
        criteria.add(context.getString(R.string.workoutMaxIntensity));
        return criteria;
    }
}
