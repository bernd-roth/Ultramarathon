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

package de.tadris.fitness.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.ui.record.RecordGpsWorkoutActivity;
import de.tadris.fitness.ui.record.RecordIndoorWorkoutActivity;
import de.tadris.fitness.ui.record.RecordWorkoutActivity;
import de.tadris.fitness.ui.workout.ShowGpsWorkoutActivity;
import de.tadris.fitness.ui.workout.ShowIndoorWorkoutActivity;
import de.tadris.fitness.ui.workout.WorkoutActivity;
import de.tadris.fitness.util.Icon;

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

    public enum RecordingType {

        INDOOR("indoor", RecordIndoorWorkoutActivity.class, ShowIndoorWorkoutActivity.class),
        GPS("gps", RecordGpsWorkoutActivity.class, ShowGpsWorkoutActivity.class);

        public final String id;
        public final Class<? extends RecordWorkoutActivity> recorderActivityClass;
        public final Class<? extends WorkoutActivity> showDetailsActivityClass;

        RecordingType(String id, Class<? extends RecordWorkoutActivity> recorderActivityClass, Class<? extends WorkoutActivity> showDetailsActivityClass) {
            this.id = id;
            this.recorderActivityClass = recorderActivityClass;
            this.showDetailsActivityClass = showDetailsActivityClass;
        }

        static RecordingType findById(String id) {
            for (RecordingType type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            return GPS;
        }
    }
}
