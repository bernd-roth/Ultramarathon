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
import de.tadris.fitness.util.unit.UnitUtils;

public class AverageSpeed extends WorkoutInformation {

    public AverageSpeed(Context context) {
        super(context);
    }

    @Override
    public String getId() {
        return "avgSpeed";
    }

    @Override
    boolean isEnabledByDefault() {
        return true;
    }

    @Override
    boolean canBeDisplayed() {
        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.workoutAvgSpeedShort);
    }

    @Override
    String getDisplayedText(WorkoutRecorder recorder) {
        return UnitUtils.getSpeed(recorder.getAvgSpeed());
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        String avgSpeed = UnitUtils.getSpeed(recorder.getAvgSpeed());
        return getString(R.string.workoutAvgSpeedLong) + ": " + avgSpeed + ".";
    }
}
