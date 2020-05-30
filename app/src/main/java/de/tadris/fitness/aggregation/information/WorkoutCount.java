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

package de.tadris.fitness.aggregation.information;

import android.content.Context;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationType;
import de.tadris.fitness.data.Workout;

public class WorkoutCount extends AbstractWorkoutInformation {
    public WorkoutCount(Context context) {
        super(context);
    }

    @Override
    public int getTitleRes() {
        return R.string.workoutNumber;
    }

    @Override
    public String getUnit() {
        return "";
    }

    @Override
    public double getValueFromWorkout(Workout workout) {
        return 1;
    }

    @Override
    public AggregationType getAggregationType() {
        return AggregationType.SUM;
    }
}