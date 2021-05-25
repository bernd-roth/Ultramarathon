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
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;

public class BurnedEnergy extends RecordingInformation {

    public BurnedEnergy(Context context) {
        super(context);
    }

    @Override
    public String getId() {
        return "energy_burned";
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
        return getString(R.string.workoutBurnedEnergy);
    }

    @Override
    String getDisplayedText(GpsWorkoutRecorder recorder) {
        return getEnergyUnitUtils().getEnergy(recorder.getCalories());
    }

    @Override
    public String getSpokenText(GpsWorkoutRecorder recorder) {
        return null;
    }
}
