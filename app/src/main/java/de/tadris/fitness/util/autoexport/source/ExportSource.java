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

public interface ExportSource {

    String EXPORT_SOURCE_WORKOUT_GPX = "workout-gpx";
    String EXPORT_SOURCE_BACKUP = "backup";

    File provideFile(Context context) throws Exception;

    static ExportSource getExportSourceByName(String name, String data) {
        switch (name) {
            case EXPORT_SOURCE_BACKUP:
                return new BackupExportSource();
            case EXPORT_SOURCE_WORKOUT_GPX:
                return new WorkoutGpxExportSource(Long.parseLong(data));
            default:
                return null;
        }
    }

}
