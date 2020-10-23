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

package de.tadris.fitness.ui.workout.diagram;

import android.content.Context;

import de.tadris.fitness.R;
import de.tadris.fitness.data.WorkoutData;
import de.tadris.fitness.data.WorkoutManager;
import de.tadris.fitness.data.WorkoutSample;

public class SpeedConverter extends AbstractSampleConverter {

    public SpeedConverter(Context context) {
        super(context);
    }

    @Override
    public void onCreate(WorkoutData data) {
        WorkoutManager.roundSpeedValues(data.getSamples());
    }

    @Override
    public float getValue(WorkoutSample sample) {
        return (float) distanceUnitUtils.getDistanceUnitSystem().getSpeedFromMeterPerSecond(sample.tmpRoundedSpeed);
    }

    @Override
    public String getName() {
        return getString(R.string.workoutSpeed);
    }

    @Override
    public String getUnit() {
        return distanceUnitUtils.getDistanceUnitSystem().getSpeedUnit();
    }

    @Override
    public boolean isIntervalSetVisible() {
        return true;
    }

    @Override
    public int getColor() {
        return R.color.diagramSpeed;
    }
}