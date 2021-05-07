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

package de.tadris.fitness.recording.announcement;

import android.content.Context;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.information.CurrentSpeed;
import de.tadris.fitness.recording.information.InformationManager;
import de.tadris.fitness.recording.information.RecordingInformation;

public class InformationAnnouncements {

    private final Context context;
    private final WorkoutRecorder recorder;
    private final TTSController TTSController;
    private final InformationManager manager;
    private long lastSpokenUpdateTime = 0;
    private int lastSpokenUpdateDistance = 0;
    private long lastSpokenSpeedWarningTime = 0;

    private float lowerTargetSpeedLimit;
    private float upperTargetSpeedLimit;

    private final long intervalTime;
    private final int intervalInMeters;
    private final long speedWarningIntervalTime;

    public InformationAnnouncements(Context context, WorkoutRecorder recorder, TTSController TTSController) {
        this.recorder = recorder;
        this.TTSController = TTSController;
        this.manager = new InformationManager(context);
        this.context = context;

        UserPreferences prefs = Instance.getInstance(context).userPreferences;
        this.intervalTime = 60 * 1000 * prefs.getSpokenUpdateTimePeriod();
        this.intervalInMeters = (int) (1000.0 / Instance.getInstance(context).distanceUnitUtils.getDistanceUnitSystem().getDistanceFromKilometers(1)
                * prefs.getSpokenUpdateDistancePeriod());
        this.speedWarningIntervalTime = 1000 * 10;

        if (prefs.hasLowerTargetSpeedLimit()) {
            lowerTargetSpeedLimit = prefs.getLowerTargetSpeedLimit();
        }
        if (prefs.hasUpperTargetSpeedLimit()) {
            upperTargetSpeedLimit = prefs.getUpperTargetSpeedLimit();
        }
    }

    public void check() {
        if (!TTSController.isTtsAvailable()) {
            return;
        } // Cannot speak

        this.checkSpeed();

        boolean shouldSpeak = false;

        if (intervalTime != 0 && recorder.getDuration() - lastSpokenUpdateTime > intervalTime) {
            shouldSpeak = true;
        }
        if (intervalInMeters != 0 && recorder.getDistanceInMeters() - lastSpokenUpdateDistance > intervalInMeters) {
            shouldSpeak = true;
        }

        if (shouldSpeak) {
            speak();
        } else {
            speakAnnouncements(false);
        }
    }

    private void checkSpeed() {
        if (speedWarningIntervalTime == 0 || recorder.getDuration() - lastSpokenSpeedWarningTime <= speedWarningIntervalTime) {
            return;
        }
        float speed = (float) recorder.getCurrentSpeed();
        if (lowerTargetSpeedLimit != 0 && lowerTargetSpeedLimit > speed) {
            TTSController.speak(context.getString(R.string.ttsBelowTargetSpeed) + ".");
            TTSController.speak(new CurrentSpeed(context).getSpokenText(recorder));
            lastSpokenSpeedWarningTime = recorder.getDuration();
        } else if (upperTargetSpeedLimit != 0 && upperTargetSpeedLimit < speed) {
            TTSController.speak(context.getString(R.string.ttsAboveTargetSpeed) + ".");
            TTSController.speak(new CurrentSpeed(context).getSpokenText(recorder));
            lastSpokenSpeedWarningTime = recorder.getDuration();
        }
    }

    private void speak() {
        speakAnnouncements(true);

        lastSpokenUpdateTime = recorder.getDuration();
        lastSpokenUpdateDistance = recorder.getDistanceInMeters();
    }

    private void speakAnnouncements(boolean playAllAnnouncements) {
        for (RecordingInformation announcement : manager.getInformation()) {
            if (playAllAnnouncements || announcement.isPlayedAlways()) {
                TTSController.speak(recorder, announcement);
            }
        }
    }
}
