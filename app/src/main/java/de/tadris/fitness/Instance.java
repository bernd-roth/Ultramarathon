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

package de.tadris.fitness;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.data.AppDatabase;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.recording.LocationListener;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.TTSController;
import de.tadris.fitness.util.FitoTrackThemes;
import de.tadris.fitness.util.UserDateTimeUtils;
import de.tadris.fitness.util.unit.DistanceUnitUtils;
import de.tadris.fitness.util.unit.EnergyUnitUtils;

public class Instance {

    private static Instance instance;
    public static Instance getInstance(Context context){
        if (context == null) {
            Log.e("Instance", "no Context Provided");
        }
        if(instance == null){
            instance= new Instance(context);
        }
        return instance;
    }


    public final AppDatabase db;
    public WorkoutRecorder recorder;
    public final List<LocationListener.LocationChangeListener> locationChangeListeners;
    public final List<TTSController.VoiceAnnouncementCallback> voiceAnnouncementCallbackListeners;
    public final UserPreferences userPreferences;
    public final FitoTrackThemes themes;
    public final UserDateTimeUtils userDateTimeUtils;
    public final DistanceUnitUtils distanceUnitUtils;
    public final EnergyUnitUtils energyUnitUtils;

    public boolean pressureAvailable= false;
    public float lastPressure= 0;

    private Instance(Context context) {
        instance = this;
        locationChangeListeners = new ArrayList<>();
        voiceAnnouncementCallbackListeners = new ArrayList<>();
        userPreferences= new UserPreferences(context);
        themes = new FitoTrackThemes(context);
        userDateTimeUtils = new UserDateTimeUtils(userPreferences);
        distanceUnitUtils = new DistanceUnitUtils(context);
        energyUnitUtils = new EnergyUnitUtils(context);
        db = AppDatabase.provideDatabase(context);

        recorder = restoreRecorder(context);
    }

    private WorkoutRecorder restoreRecorder(Context context){
        Workout lastWorkout = db.workoutDao().getLastWorkout();
        if (lastWorkout != null && lastWorkout.end == -1){
            List<WorkoutSample> samples = Arrays.asList(db.workoutDao().getAllSamplesOfWorkout(lastWorkout.id));
            return new WorkoutRecorder(context, lastWorkout, samples);
        }
        return new WorkoutRecorder(context, WorkoutType.OTHER);
    }
}
