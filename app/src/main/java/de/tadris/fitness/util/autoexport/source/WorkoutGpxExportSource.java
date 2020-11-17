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

package de.tadris.fitness.util.autoexport.source;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutData;
import de.tadris.fitness.util.DataManager;
import de.tadris.fitness.util.io.general.IOHelper;

public class WorkoutGpxExportSource implements ExportSource {

    private final long workoutId;

    public WorkoutGpxExportSource(long workoutId) {
        this.workoutId = workoutId;
    }

    @Override
    public File provideFile(Context context) throws Exception {
        Workout workout = Instance.getInstance(context).db.workoutDao().getWorkoutById(workoutId);
        WorkoutData data = WorkoutData.fromWorkout(context, workout);
        String file = DataManager.getSharedDirectory(context) + String.format("/workout-%s-%s.gpx", workout.getSafeDateString(), workout.getSafeComment());
        File parent = new File(file).getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot write to " + file);
        }

        IOHelper.GpxExporter.exportWorkout(data, new File(file));
        return new File(file);
    }

}
