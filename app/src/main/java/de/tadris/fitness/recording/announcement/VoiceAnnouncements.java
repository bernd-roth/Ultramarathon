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

import java.util.List;

import de.tadris.fitness.data.Interval;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.interval.IntervalAnnouncements;

public class VoiceAnnouncements {

    private InformationAnnouncements informationAnnouncements;
    private IntervalAnnouncements intervalAnnouncements;

    public VoiceAnnouncements(Context context, WorkoutRecorder recorder, TTSController ttsController, List<Interval> intervals) {
        this.informationAnnouncements= new InformationAnnouncements(context, recorder, ttsController);
        this.intervalAnnouncements = new IntervalAnnouncements(context, recorder, ttsController, intervals);
    }

    public void check(){
        intervalAnnouncements.check();
        informationAnnouncements.check();
    }

    public void applyIntervals(List<Interval> intervals) {
        intervalAnnouncements.setIntervals(intervals);
    }

}
