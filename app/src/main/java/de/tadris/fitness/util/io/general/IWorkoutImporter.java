package de.tadris.fitness.util.io.general;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;

public interface IWorkoutImporter {
    class WorkoutImportResult
    {
        public Workout workout;
        public List<WorkoutSample> samples;

        public WorkoutImportResult(Workout workout, List<WorkoutSample> samples) {
            this.workout = workout;
            this.samples = samples;
        }
    }

    WorkoutImportResult readWorkout(InputStream input) throws IOException;

    default void importWorkout(Context context, InputStream input) throws IOException {
        WorkoutImportResult importResult = readWorkout(input);
        new ImportWorkoutSaver(context, importResult.workout, importResult.samples).saveWorkout();
    }

    default void importWorkout(Context context, File file) throws IOException {
        importWorkout(context, new FileInputStream(file));
    }
}
