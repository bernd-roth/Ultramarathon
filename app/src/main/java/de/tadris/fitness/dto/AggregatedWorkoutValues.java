package de.tadris.fitness.dto;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.data.Workout;

public class AggregatedWorkoutValues {
    private double fastestAverage;
    private int greatestDistance;
    private String fastestAverageDate;
    private String greatestDistanceDate;
    private final ArrayList<DataPointAverageSpeed> averageSpeedValues;
    private final ArrayList<DataPointDistance> distanceValues;

    public AggregatedWorkoutValues(Workout[] workouts) {
        fastestAverage = greatestDistance = 0;
        fastestAverageDate = greatestDistanceDate = "--";
        averageSpeedValues = new ArrayList<>();
        distanceValues = new ArrayList<>();
        for (Workout workout : workouts) {

            if (fastestAverage < workout.getAvgSpeedTotal()) {
                fastestAverage = workout.getAvgSpeedTotal();
                fastestAverageDate = workout.getDateString();
            }

            if (greatestDistance < workout.length) {
                greatestDistance = workout.length;
                greatestDistanceDate = workout.getDateString();
            }

            float timepoint = TimeUnit.MILLISECONDS.toHours(workout.end);
            averageSpeedValues.add(new DataPointAverageSpeed(timepoint, (float) workout.getAvgSpeedTotal()));
            distanceValues.add(new DataPointDistance(timepoint, workout.length));
        }
    }

    public double getFastestAverage() {
        return fastestAverage;
    }

    public int getGreatestDistance() {
        return greatestDistance;
    }

    public String getFastestAverageDate() {
        return fastestAverageDate;
    }

    public String getGreatestDistanceDate() {
        return greatestDistanceDate;
    }

    public ArrayList<DataPointAverageSpeed> getAverageSpeedData() {
        return averageSpeedValues;
    }

    public ArrayList<DataPointDistance> getDistanceData() {
        return distanceValues;
    }
}
