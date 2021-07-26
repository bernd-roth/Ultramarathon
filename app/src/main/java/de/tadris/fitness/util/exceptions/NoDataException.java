package de.tadris.fitness.util.exceptions;

import de.tadris.fitness.data.WorkoutType;

/**
 * This Exception indicates that there is no data for the corresponding WorkoutType
 */
public class NoDataException extends Exception {
    private WorkoutType workoutType;

    public NoDataException(WorkoutType workoutType) {
        this.workoutType = workoutType;
    }

    public WorkoutType getWorkoutType() {
        return workoutType;
    }
}
