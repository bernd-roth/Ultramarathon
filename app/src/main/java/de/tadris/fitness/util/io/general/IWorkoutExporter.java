package de.tadris.fitness.util.io.general;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;

public interface IWorkoutExporter {
    void exportWorkout(Workout workout, List<WorkoutSample> samples, OutputStream outputStream) throws IOException;

    default void exportWorkout(Workout workout, List<WorkoutSample> samples, File file) throws IOException
    {
        exportWorkout(workout, samples, new FileOutputStream(file));
    }
}
