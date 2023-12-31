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

package de.tadris.fitness.recording.information;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.data.RecordingType;

public class InformationManager {

    private final Context context;
    private final List<RecordingInformation> information = new ArrayList<>();

    public InformationManager(RecordingType mode, Context context) {
        this.context = context;
        addInformation(mode);
    }

    private void addInformation(RecordingType mode) {
        information.add(new SystemActions(context));
        information.add(new CurrentTime(context));
        information.add(new Duration(context));
        information.add(new PauseDuration(context));
        if (mode == RecordingType.GPS) {
            information.add(new GPSStatus(context));
            information.add(new Distance(context));
            information.add(new CurrentSpeed(context));
            information.add(new SpeedLastMinute(context));
            information.add(new AverageSpeedMotion(context));
            information.add(new AverageSpeedTotal(context));
            information.add(new AveragePace(context));
            information.add(new Ascent(context));
        }
        if (mode == RecordingType.INDOOR) {
            information.add(new Repetitions(context));
            information.add(new CurrentIntensity(context));
            information.add(new CurrentFrequency(context));
            information.add(new AverageFrequency(context));
        }
        information.add(new CurrentHeartRate(context));
        information.add(new CurrentHRBatteryLevel(context));
        information.add(new BurnedEnergy(context));
    }

    public RecordingInformation getInformationById(String id) {
        for (RecordingInformation information : this.information) {
            if (information.getId().equals(id)) {
                return information;
            }
        }
        return null;
    }

    public List<RecordingInformation> getDisplayableInformation() {
        List<RecordingInformation> displayable = new ArrayList<>();
        for (RecordingInformation information : this.information) {
            if (information.canBeDisplayed()) {
                displayable.add(information);
            }
        }
        return displayable;
    }

    public List<RecordingInformation> getInformation() {
        return information;
    }
}
