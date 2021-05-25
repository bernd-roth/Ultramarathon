package de.tadris.fitness.recording.indoor;

import android.content.Context;

import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.IndoorSample;
import de.tadris.fitness.data.IndoorWorkout;
import de.tadris.fitness.data.IndoorWorkoutData;

public class IndoorWorkoutSaver {

    private final Context context;
    private final IndoorWorkout workout;
    private final List<IndoorSample> samples;

    public IndoorWorkoutSaver(Context context, IndoorWorkoutData workoutData) {
        this.context = context;
        this.workout = workoutData.getWorkout();
        this.samples = workoutData.getSamples();
    }

    public void save() {

    }

    private void calculateData() {

    }

    private void insertWorkoutAndSamples() {
        Instance.getInstance(context).db.indoorWorkoutDao().insertWorkoutAndSamples(workout, samples.toArray(new IndoorSample[0]));
    }

}
