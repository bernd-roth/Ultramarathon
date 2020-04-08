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

package de.tadris.fitness.recording.announcement;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.recording.announcement.information.AnnouncementAverageSpeed;
import de.tadris.fitness.recording.announcement.information.AnnouncementDistance;
import de.tadris.fitness.recording.announcement.information.AnnouncementDuration;
import de.tadris.fitness.recording.announcement.information.AnnouncementGPSStatus;
import de.tadris.fitness.recording.announcement.information.InformationAnnouncement;

public class AnnouncementManager {

    private Context context;
    private List<InformationAnnouncement> announcements = new ArrayList<>();

    public AnnouncementManager(Context context) {
        this.context = context;
        addAnnouncements();
    }

    private void addAnnouncements() {
        announcements.add(new AnnouncementGPSStatus(context));
        announcements.add(new AnnouncementDuration(context));
        announcements.add(new AnnouncementDistance(context));
        announcements.add(new AnnouncementAverageSpeed(context));
    }

    public List<InformationAnnouncement> getAnnouncements() {
        return announcements;
    }
}
