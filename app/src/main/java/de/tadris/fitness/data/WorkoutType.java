/*
 * Copyright (c) 2022 Jannis Scheibe <jannis@tadris.de>
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

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

@Entity(tableName = "workout_type")
public class WorkoutType implements Serializable {

    @PrimaryKey
    @NonNull
    public String id;

    public String title;

    @ColumnInfo(name = "min_distance")
    public int minDistance;

    public int color;

    public String icon;

    @ColumnInfo(name = "met")
    public int MET;

    @ColumnInfo(name = "type")
    public String recordingType;

    @Ignore
    @PluralsRes
    // Only for indoor workouts
    // treadmill -> "steps"
    public int repeatingExerciseName;

    @Ignore
    public WorkoutType(@NonNull String id, String title, int minDistance, int color, String icon, int MET, String recordingType) {
        this(id, title, minDistance, color, icon, MET, recordingType, -1);
    }

    @Ignore
    public WorkoutType(@NonNull String id, String title, int minDistance, int color, String icon, int MET, String recordingType, int repeatingExerciseName) {
        this.id = id;
        this.title = title;
        this.minDistance = minDistance;
        this.color = color;
        this.icon = icon;
        this.MET = MET;
        this.recordingType = recordingType;
        this.repeatingExerciseName = repeatingExerciseName;
    }

    public WorkoutType() {
    }

    @Ignore
    @JsonIgnore
    public RecordingType getRecordingType() {
        return RecordingType.findById(this.recordingType);
    }
}
