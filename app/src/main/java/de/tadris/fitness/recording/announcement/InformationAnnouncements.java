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

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.information.InformationManager;
import de.tadris.fitness.recording.information.RecordingInformation;

public class InformationAnnouncements {

    private final WorkoutRecorder recorder;
    private final TTSController TTSController;
    private final InformationManager manager;
    private long lastSpokenUpdateTime = 0;
    private int lastSpokenUpdateDistance = 0;

    private final long intervalTime;
    private final int intervalInMeters;

    public InformationAnnouncements(Context context, WorkoutRecorder recorder, TTSController TTSController){
        this.recorder= recorder;
        this.TTSController = TTSController;
        this.manager = new InformationManager(context);

        UserPreferences prefs = Instance.getInstance(context).userPreferences;
        this.intervalTime = 60 * 1000 * prefs.getSpokenUpdateTimePeriod();
        this.intervalInMeters = (int) (1000.0 / Instance.getInstance(context).distanceUnitUtils.getDistanceUnitSystem().getDistanceFromKilometers(1)
                * prefs.getSpokenUpdateDistancePeriod());
    }

    public void check() {
        if (!TTSController.isTtsAvailable()) {
            return;
        } // Cannot speak

        boolean shouldSpeak = false;

        if (intervalTime != 0 && recorder.getDuration() - lastSpokenUpdateTime > intervalTime) {
            shouldSpeak = true;
        }
        if (intervalInMeters != 0 && recorder.getDistanceInMeters() - lastSpokenUpdateDistance > intervalInMeters) {
            shouldSpeak = true;
        }

        if (shouldSpeak) {
            speak();
        }
    }

    private void speak() {
        for (RecordingInformation announcement : manager.getInformation()) {
            TTSController.speak(recorder, announcement);
        }

        lastSpokenUpdateTime = recorder.getDuration();
        lastSpokenUpdateDistance = recorder.getDistanceInMeters();
    }

}
