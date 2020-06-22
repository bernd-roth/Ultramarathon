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

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import java.io.Serializable;

import de.tadris.fitness.R;

public enum WorkoutType implements Serializable {

    RUNNING("running", R.string.workoutTypeRunning, 5, true, R.drawable.ic_run, R.color.colorPrimaryRunning, R.style.Running, R.style.RunningDark),
    WALKING("walking", R.string.workoutTypeWalking, 5, true, R.drawable.ic_walk, R.color.colorPrimaryRunning, R.style.Running, R.style.RunningDark),
    HIKING("hiking", R.string.workoutTypeHiking, 5, true, R.drawable.ic_walk, R.color.colorPrimaryHiking, R.style.Hiking, R.style.HikingDark),
    CYCLING("cycling", R.string.workoutTypeCycling, 10, true, R.drawable.ic_bike, R.color.colorPrimaryBicyclingLighter, R.style.Bicycling, R.style.BicyclingDark),
    INLINE_SKATING("inline_skating", R.string.workoutTypeInlineSkating, 7, true, R.drawable.ic_inline_skating, R.color.colorPrimaryInlineSkating, R.style.InlineSkating, R.style.InlineSkatingDark),
    SKATEBOARDING("skateboarding", R.string.workoutTypeSkateboarding, 7, true, R.drawable.ic_skateboarding, R.color.colorPrimaryInlineSkating, R.style.InlineSkating, R.style.InlineSkatingDark),
    OTHER("other", R.string.workoutTypeOther, 7, true, R.drawable.ic_other, R.color.colorPrimary, R.style.AppTheme, R.style.AppThemeDark);

    public String id;
    @StringRes
    public int title;
    public int minDistance; // Minimum distance between samples
    public boolean hasGPS;
    @StyleRes
    public int lightTheme, darkTheme;
    @DrawableRes
    public int icon;
    @ColorRes
    public int color;

    WorkoutType(String id, int title, int minDistance, boolean hasGPS, int icon, int color, int lightTheme, int darkTheme) {
        this.id = id;
        this.title = title;
        this.minDistance = minDistance;
        this.hasGPS = hasGPS;
        this.icon = icon;
        this.color = color;
        this.lightTheme = lightTheme;
        this.darkTheme = darkTheme;
    }

    public static WorkoutType getTypeById(String id) {
        for (WorkoutType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return OTHER;
    }
}
