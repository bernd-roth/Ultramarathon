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

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.data.BaseWorkout;
import de.tadris.fitness.data.IndoorSample;
import de.tadris.fitness.data.IndoorWorkout;
import de.tadris.fitness.data.IndoorWorkoutData;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.recording.BaseWorkoutRecorder;
import de.tadris.fitness.ui.record.RecordIndoorWorkoutActivity;
import de.tadris.fitness.ui.record.RecordWorkoutActivity;

public class IndoorWorkoutRecorder extends BaseWorkoutRecorder {

    IndoorWorkout workout;
    List<IndoorSample> samples = new ArrayList<>();
    private boolean saved = false;

    public IndoorWorkoutRecorder(Context context, WorkoutType workoutType) {
        super(context);
        workout = new IndoorWorkout();
        workout.edited = false;
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
        return 0; // TODO
    }

    @Override
    public Class<? extends RecordWorkoutActivity> getActivityClass() {
        return RecordIndoorWorkoutActivity.class;
    }
}
