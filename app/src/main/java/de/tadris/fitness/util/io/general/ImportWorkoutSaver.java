/*
 * Copyright (c) 2020 Jannis Scheibe <jannis@tadris.de>
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

package de.tadris.fitness.util.io.general;

import android.content.Context;

import java.util.List;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.recording.WorkoutSaver;

public class ImportWorkoutSaver extends WorkoutSaver {

    public ImportWorkoutSaver(Context context, Workout workout, List<WorkoutSample> samples) {
        super(context, workout, samples);
    }

    public void saveWorkout(){
        setIds();
        setSimpleValues();
        setSpeed();
        setTopSpeed();

        setMSLElevation();
        setAscentAndDescent();

        setCalories();

        storeInDatabase();
    }

    private void setSpeed() {
        setTopSpeed();
        if (samples.size() == 0) {
            return;
        }
        if (workout.topSpeed != 0) {
            // Speed values already present
            return;
        }
        WorkoutSample lastSample = samples.get(0);
        for(WorkoutSample sample : samples){
            double distance = lastSample.toLatLong().sphericalDistance(sample.toLatLong());
            long timeDiff = sample.absoluteTime - lastSample.absoluteTime;
            if (timeDiff != 0) {
                sample.speed = distance / ((double) timeDiff / 1000);
            }
            lastSample = sample;
        }
    }

}
