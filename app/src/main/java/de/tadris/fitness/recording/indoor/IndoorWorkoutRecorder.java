/*
 * Copyright (c) 2021 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tadris.fitness.recording.indoor;

import android.content.Context;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.data.BaseWorkout;
import de.tadris.fitness.data.IndoorSample;
import de.tadris.fitness.data.IndoorWorkout;
import de.tadris.fitness.data.IndoorWorkoutData;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.recording.BaseWorkoutRecorder;
import de.tadris.fitness.recording.indoor.exercise.ExerciseRecognizer;
import de.tadris.fitness.ui.record.RecordIndoorWorkoutActivity;
import de.tadris.fitness.ui.record.RecordWorkoutActivity;
import de.tadris.fitness.util.CalorieCalculator;

public class IndoorWorkoutRecorder extends BaseWorkoutRecorder {

    WorkoutType type;
    IndoorWorkout workout;
    IndoorSample lastSample;
    final List<IndoorSample> samples = new ArrayList<>();
    private boolean saved = false;

    private int repetitions = 0;

    public IndoorWorkoutRecorder(Context context, WorkoutType workoutType) {
        super(context);
        this.type = workoutType;
        this.workout = new IndoorWorkout();
        this.workout.edited = false;
        this.workout.setWorkoutType(workoutType);
    }

    @Override
    public boolean hasRecordedSomething() {
        return samples.size() > 2;
    }

    @Override
    protected void onWatchdog() {

    }

    @Override
    protected boolean autoPausePossible() {
        return true;
    }

    @Override
    protected void onStart() {
        workout.id = System.nanoTime();
        workout.start = System.currentTimeMillis();
        //Init Workout To Be able to Save
        workout.end = -1;
    }

    @Override
    protected void onStop() {
        workout.end = System.currentTimeMillis();
        workout.duration = time;
        workout.pauseDuration = pauseTime;
    }

    @Subscribe
    public void onRepetitionRecognized(ExerciseRecognizer.RepetitionRecognizedEvent event) {
        lastSampleTime = System.currentTimeMillis();
        if (state == RecordingState.RUNNING && event.getTimestamp() > workout.start) {
            Log.d("Recorder", "repetition recognized with intensity " + event.getIntensity());
            if (lastSample != null && lastSample.repetitions < type.minDistance && event.getTimestamp() - lastSample.absoluteTime < PAUSE_TIME) {
                addToExistingSample(event);
            } else {
                addNewSample(event);
            }
            repetitions++;
        }
    }

    private void addToExistingSample(ExerciseRecognizer.RepetitionRecognizedEvent event) {
        lastSample.intensity = (lastSample.repetitions * lastSample.intensity + event.getIntensity()) / (lastSample.repetitions + 1);
        lastSample.absoluteEndTime = event.getTimestamp();
        lastSample.repetitions++;
    }

    private void addNewSample(ExerciseRecognizer.RepetitionRecognizedEvent event) {
        if (lastSample != null) {
            // lastSample will not be changed further so we broadcast it
            EventBus.getDefault().post(lastSample);
        }

        IndoorSample sample = new IndoorSample();
        sample.absoluteTime = event.getTimestamp();
        sample.absoluteEndTime = event.getTimestamp();
        sample.repetitions = 1;
        sample.relativeTime = event.getTimestamp() - startTime - getPauseDuration();
        sample.intensity = event.getIntensity();
        sample.heartRate = lastHeartRate;
        sample.intervalTriggered = lastTriggeredInterval;
        lastTriggeredInterval = -1;
        samples.add(sample);
        lastSample = sample;
    }

    public int getRepetitionsTotal() {
        return repetitions;
    }

    private IndoorWorkoutData getWorkoutData() {
        return new IndoorWorkoutData(workout, samples);
    }

    @Override
    public void save() {
        new IndoorWorkoutSaver(context, getWorkoutData()).save();
        saved = true;
    }

    @Override
    public boolean isSaved() {
        return saved;
    }

    @Override
    public void discard() {
    }

    @Override
    public BaseWorkout getWorkout() {
        return workout;
    }

    @Override
    public int getCalories() {
        workout.duration = getDuration();
        return CalorieCalculator.calculateCalories(context, workout);
    }

    public List<IndoorSample> getSamples() {
        return new ArrayList<>(samples);
    }

    @Override
    public Class<? extends RecordWorkoutActivity> getActivityClass() {
        return RecordIndoorWorkoutActivity.class;
    }
}
