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

package de.tadris.fitness.ui.workout.diagram;

import android.content.Context;

import de.tadris.fitness.R;
import de.tadris.fitness.data.BaseSample;
import de.tadris.fitness.data.BaseWorkout;
import de.tadris.fitness.data.BaseWorkoutData;
import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.data.WorkoutManager;

public class SpeedConverter extends AbstractSampleConverter {

    public SpeedConverter(Context context) {
        super(context);
    }

    @Override
    public void onCreate(BaseWorkoutData data) {
        WorkoutManager.recalculateSpeedValues(data.castToGpsData().getSamples());
        WorkoutManager.roundSpeedValues(data.castToGpsData().getSamples());
    }

    @Override
    public float getValue(BaseSample sample) {
        return (float) distanceUnitUtils.getDistanceUnitSystem().getSpeedFromMeterPerSecond(((GpsSample) sample).tmpRoundedSpeed);
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

    @Override
    public float getMinValue(BaseWorkout workout) {
        return (float) distanceUnitUtils.getDistanceUnitSystem().getSpeedFromMeterPerSecond(((GpsWorkout) workout).avgSpeed * 0.4);
    }

    @Override
    public float getMaxValue(BaseWorkout workout) {
        return (float) distanceUnitUtils.getDistanceUnitSystem().getSpeedFromMeterPerSecond(((GpsWorkout) workout).avgSpeed * 1.6);
    }
}
