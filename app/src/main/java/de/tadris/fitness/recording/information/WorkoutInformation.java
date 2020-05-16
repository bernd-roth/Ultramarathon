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
import android.preference.PreferenceManager;

import androidx.annotation.StringRes;

import de.tadris.fitness.Instance;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.Announcement;
import de.tadris.fitness.util.unit.DistanceUnitUtils;
import de.tadris.fitness.util.unit.EnergyUnitUtils;

public abstract class WorkoutInformation implements Announcement {

    private Context context;

    WorkoutInformation(Context context) {
        this.context = context;
    }

    public boolean isAnnouncementEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("announcement_" + getId(), isEnabledByDefault());
    }

    protected String getString(@StringRes int resId) {
        return context.getString(resId);
    }

    protected DistanceUnitUtils getDistanceUnitUtils() {
        return Instance.getInstance(context).distanceUnitUtils;
    }

    protected EnergyUnitUtils getEnergyUnitUtils() {
        return Instance.getInstance(context).energyUnitUtils;
    }

    public abstract String getId();

    abstract boolean isEnabledByDefault();

    abstract boolean canBeDisplayed();

    public abstract String getTitle();

    abstract String getDisplayedText(WorkoutRecorder recorder);

}