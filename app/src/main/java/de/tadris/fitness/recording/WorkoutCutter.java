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

package de.tadris.fitness.recording;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import de.tadris.fitness.data.WorkoutData;
import de.tadris.fitness.data.WorkoutSample;

public class WorkoutCutter extends WorkoutSaver {

    public WorkoutCutter(Context context, WorkoutData data) {
        super(context, data);
    }

    public void cutWorkout(@Nullable WorkoutSample startSample, @Nullable WorkoutSample endSample) {
        if (startSample != null) {
            cutStart(startSample);
        }
        if (endSample != null) {
            cutEnd(endSample);
        }
        calculateDurations(); // Recalculate start, end, duration, pause duration
        calculateData(false); // Recalculate data

        updateWorkoutAndSamples();
    }

    private void cutStart(WorkoutSample startSample) {
        for (WorkoutSample sample : new ArrayList<>(samples)) {
            if (sample.id == startSample.id) {
                break;
            } else {
                samples.remove(sample);

            }
        }
        // Move relative times
        long startTime = startSample.relativeTime;
        for (WorkoutSample sample : samples) {
            sample.relativeTime -= startTime;
        }
    }

    private void cutEnd(WorkoutSample endSample) {
        boolean found = false;
        for (WorkoutSample sample : new ArrayList<>(samples)) {
            if (found) {
                deleteSample(sample);
            } else if (sample.id == endSample.id) {
                found = true;
            }
        }
    }

    private void deleteSample(WorkoutSample sample) {
        samples.remove(sample);
        db.workoutDao().deleteSample(sample);
    }

}
