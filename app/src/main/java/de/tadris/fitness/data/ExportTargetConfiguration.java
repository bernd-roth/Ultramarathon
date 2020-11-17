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

package de.tadris.fitness.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import de.tadris.fitness.util.autoexport.target.DirectoryTarget;
import de.tadris.fitness.util.autoexport.target.ExportTarget;

@Entity(tableName = "export_target_config")
public class ExportTargetConfiguration {

    public static final String TARGET_TYPE_DIRECTORY = "directory";

    @PrimaryKey(autoGenerate = true)
    public long id;

    /**
     * @see de.tadris.fitness.util.autoexport.source.ExportSource
     */
    public String source;

    public String type;

    public String data;

    public ExportTarget getTargetImplementation() {
        switch (type) {
            case TARGET_TYPE_DIRECTORY:
                return new DirectoryTarget(data);
            default:
                return null;
        }
    }

}
