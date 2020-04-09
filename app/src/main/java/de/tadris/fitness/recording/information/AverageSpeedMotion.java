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

package de.tadris.fitness.recording.information;

import android.content.Context;

import de.tadris.fitness.R;
import de.tadris.fitness.recording.WorkoutRecorder;

public class AverageSpeedMotion extends WorkoutInformation {

    public AverageSpeedMotion(Context context) {
        super(context);
    }

    @Override
    public String getId() {
        return "avgSpeedMotion";
    }

    @Override
    boolean isEnabledByDefault() {
        return false;
    }

    @Override
    boolean canBeDisplayed() {
        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.avgSpeedInMotion);
    }

    @Override
    String getDisplayedText(WorkoutRecorder recorder) {
        return getDistanceUnitUtils().getSpeed(recorder.getAvgSpeed());
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        String avgSpeed = getDistanceUnitUtils().getSpeed(recorder.getAvgSpeed(), true);
        return getString(R.string.workoutAvgSpeedLong) + ": " + avgSpeed + ".";
    }
}
