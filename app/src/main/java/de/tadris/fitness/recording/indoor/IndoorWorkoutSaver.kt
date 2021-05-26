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
        calculateData();
        insertWorkoutAndSamples();
    }

    private void calculateData() {

    }

    private void insertWorkoutAndSamples() {
        Instance.getInstance(context).db.indoorWorkoutDao().insertWorkoutAndSamples(workout, samples.toArray(new IndoorSample[0]));
    }

}
