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

import java.util.ArrayList;
import java.util.List;

public class InformationManager {

    private Context context;
    private List<WorkoutInformation> information = new ArrayList<>();

    public InformationManager(Context context) {
        this.context = context;
        addInformation();
    }

    private void addInformation() {
        information.add(new GPSStatus(context));
        information.add(new Distance(context));
        information.add(new Duration(context));
        information.add(new PauseDuration(context));
        information.add(new AverageSpeedMotion(context));
        information.add(new AverageSpeedTotal(context));
        information.add(new CurrentSpeed(context));
        information.add(new Ascent(context));
        information.add(new BurnedEnergy(context));
        information.add(new CurrentTime(context));
    }

    public WorkoutInformation getInformationById(String id) {
        for (WorkoutInformation information : this.information) {
            if (information.getId().equals(id)) {
                return information;
            }
        }
        return null;
    }

    public List<WorkoutInformation> getDisplayableInformation() {
        List<WorkoutInformation> displayable = new ArrayList<>();
        for (WorkoutInformation information : this.information) {
            if (information.canBeDisplayed()) {
                displayable.add(information);
            }
        }
        return displayable;
    }

    public List<WorkoutInformation> getInformation() {
        return information;
    }
}
