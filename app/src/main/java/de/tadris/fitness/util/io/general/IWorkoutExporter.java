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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.GpsWorkout;

public interface IWorkoutExporter {
    void exportWorkout(GpsWorkout workout, List<GpsSample> samples, OutputStream outputStream) throws IOException;

    default void exportWorkout(GpsWorkout workout, List<GpsSample> samples, File file) throws IOException {
        exportWorkout(workout, samples, new FileOutputStream(file));
    }
}
