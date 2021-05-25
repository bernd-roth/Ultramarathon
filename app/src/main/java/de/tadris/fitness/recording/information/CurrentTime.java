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

import java.util.Calendar;
import java.util.Locale;

import de.tadris.fitness.R;
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;

public class CurrentTime extends RecordingInformation {

    public CurrentTime(Context context) {
        super(context);
    }

    @Override
    public String getId() {
        return "currentTime";
    }

    @Override
    boolean isEnabledByDefault() {
        return false;
    }

    @Override
    public String getSpokenText(GpsWorkoutRecorder recorder) {
        return getString(R.string.currentTime) + ": " + getSpokenTime(Calendar.getInstance(Locale.getDefault())) + ".";
    }

    private String getSpokenTime(Calendar currentTime) {

        StringBuilder spokenTime = new StringBuilder();

        long hours = currentTime.get(Calendar.HOUR_OF_DAY);
        spokenTime.append(hours).append(" ");
        spokenTime.append(getString(R.string.oClock)).append(" ")
                .append(getString(R.string.and)).append(" ");
        long minutes = currentTime.get(Calendar.MINUTE);
        spokenTime.append(minutes).append(" ");
        spokenTime.append(getString(minutes == 1 ? R.string.timeMinuteSingular : R.string.timeMinutePlural));

        return spokenTime.toString();
    }

    @Override
    boolean canBeDisplayed() {
        return false;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    String getDisplayedText(GpsWorkoutRecorder recorder) {
        return null;
    }
}
