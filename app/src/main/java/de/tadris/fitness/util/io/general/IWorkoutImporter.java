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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;

public interface IWorkoutImporter {
    class WorkoutImportResult {
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
