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

package de.tadris.fitness.recording;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.BuildConfig;
import de.tadris.fitness.Instance;
import de.tadris.fitness.data.Interval;
import de.tadris.fitness.data.IntervalSet;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.CalorieCalculator;

public class WorkoutRecorder implements LocationListener.LocationChangeListener {

    private static final int PAUSE_TIME = 10_000; // 10 Seconds

    /**
     * Time after which the workout is stopped and saved automatically because there is no activity anymore
     */
    private static final int AUTO_TIMEOUT_MULTIPLIER = 1_000 * 60; // minutes to ms
    private static final int DEFAULT_WORKOUT_AUTO_TIMEOUT = 20;

    private final long autoTimeout;
    private final Context context;
    private final Workout workout;
    private final List<WorkoutSample> samples = new ArrayList<>();
    private final WorkoutSaver workoutSaver;
    private RecordingState state;
    private long time = 0;
    private long pauseTime = 0;
    private long lastResume;
    private long lastPause = 0;
    private long lastSampleTime = 0;
    private double distance = 0;

    private boolean saved = false;

    private static final double SIGNAL_BAD_THRESHOLD = 30; // In meters
    private static final int SIGNAL_LOST_THRESHOLD = 10_000; // 10 Seconds In milliseconds
    private Location lastFix = null;
    private final List<WorkoutRecorderListener> workoutRecorderListeners = new ArrayList<>(); // Only synchronized access
    private GpsState gpsState = GpsState.SIGNAL_LOST;
    private List<Interval> intervalList;

    public WorkoutRecorder(Context context, WorkoutType workoutType) {
        this.context = context;
        this.state = RecordingState.IDLE;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.autoTimeout = prefs.getInt("autoTimeoutPeriod", DEFAULT_WORKOUT_AUTO_TIMEOUT) * AUTO_TIMEOUT_MULTIPLIER;

        this.workout = new Workout();
        workout.edited = false;

        // Default values
        this.workout.comment = "";
        this.workout.intervalSetIncludesPauses = Instance.getInstance(context).userPreferences.intervalsIncludePauses();

        this.workout.setWorkoutType(workoutType);

        workoutSaver = new WorkoutSaver(this.context, workout, samples);

        init();
    }

    public WorkoutRecorder(Context context, Workout workout, List<WorkoutSample> samples){
        this.context = context;
        this.state = RecordingState.PAUSED;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.autoTimeout = prefs.getInt("autoTimeoutPeriod", DEFAULT_WORKOUT_AUTO_TIMEOUT) * AUTO_TIMEOUT_MULTIPLIER;

        this.workout = workout;
        this.samples.addAll(samples);

        // time = 0; x
        // pauseTime = 0; x
        // lastResume; x
        // lastPause = 0; x
        // lastSampleTime = 0; x
        // distance = 0; x
        reconstructBySamples();

        workoutSaver = new WorkoutSaver(this.context, this.workout, this.samples);
        init();
    }

    private void reconstructBySamples(){
        lastResume = workout.start;
        lastSampleTime = workout.start;
        LatLong prefLocation = null;
        for(WorkoutSample sample: samples){
            long timeDiff = sample.absoluteTime - lastSampleTime;
            if (timeDiff > PAUSE_TIME){ // Handle Pause
                lastPause = lastSampleTime+PAUSE_TIME; // Also add the Minimal Pause Time ;D
                lastResume = sample.absoluteTime; // Workout resumed at new sample
                pauseTime += timeDiff-PAUSE_TIME; // Add Time Diff without Pause Time
            }
            if(prefLocation!=null){ //Update Distance
                double sampleDistance = prefLocation.sphericalDistance(sample.toLatLong());
                distance+=sampleDistance;
            }
            prefLocation = sample.toLatLong();
            lastSampleTime = sample.absoluteTime;
            time = sample.relativeTime; // Update Times Always To Sample RelTime
        }
        if(System.currentTimeMillis()-lastSampleTime > PAUSE_TIME) {
            state = RecordingState.PAUSED;
            time += time + PAUSE_TIME;
            lastPause = lastSampleTime + PAUSE_TIME;
        }else{
            state = RecordingState.RUNNING;
        }
    }

    public Workout getWorkout() {
        return this.workout;
    }

    public GpsState getGpsState() {
        return this.gpsState;
    }

    public List<WorkoutSample> getSamples() {
        return this.samples;
    }

    private void init() {
        Instance.getInstance(context).locationChangeListeners.add(this);
    }

    public void start() {
        if (state == RecordingState.IDLE) {
            Log.i("Recorder", "Start");
            workout.id = System.currentTimeMillis();
            workout.start = System.currentTimeMillis();
            //Init Workout To Be able to Save
            workout.end = -1L;
            workout.avgSpeed = -1d;
            workout.topSpeed = -1d;
            workout.ascent = -1f;
            workout.descent = -1f;
            workoutSaver.storeWorkoutInDatabase(); // Already Persist Workout
            resume();
        } else if (state == RecordingState.PAUSED) {
            resume();
        } else if (state != RecordingState.RUNNING) {
            throw new IllegalStateException("Cannot start or resume recording. state = " + state);
        }
    }

    public boolean isActive() {
        return state == RecordingState.IDLE || state == RecordingState.RUNNING || state == RecordingState.PAUSED;
    }

    public boolean isResumed() {
        return state == RecordingState.RUNNING;
    }

    /**
     * Handles the Record Watchdog, for GPS Check, Pause Detection and Auto Timeout
     *
     * @return is still active workout
     */
    boolean handleWatchdog() {
        if(BuildConfig.DEBUG) {
            Log.d("WorkoutRecorder", "handleWatchdog " + this.getState().toString() + " samples: " + samples.size() + " autoTout: " + autoTimeout + " inst: " + this.toString());
        }
        if (isActive()) {
            checkSignalState();
            synchronized (samples) {
                if (samples.size() > 2) {
                    long timeDiff = System.currentTimeMillis() - lastSampleTime;
                    if (autoTimeout > 0 && timeDiff > autoTimeout) {
                        if (isActive()) {
                            stop();
                            save();
                            synchronized (workoutRecorderListeners) {
                                for (WorkoutRecorderListener listener : workoutRecorderListeners) {
                                    listener.onAutoStop();
                                }
                            }
                        }
                    } else if (timeDiff > PAUSE_TIME) {
                        if (state == RecordingState.RUNNING && gpsState != GpsState.SIGNAL_LOST) {
                            pause();
                        }
                    } else {
                        if (state == RecordingState.PAUSED) {
                            resume();
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void checkSignalState() {
        if (lastFix == null) {
            return;
        }
        GpsState state;
        if (System.currentTimeMillis() - lastFix.getTime() > SIGNAL_LOST_THRESHOLD) {
            state = GpsState.SIGNAL_LOST;
        } else if (lastFix.getAccuracy() > SIGNAL_BAD_THRESHOLD) {
            state = GpsState.SIGNAL_BAD;
        } else {
            state = GpsState.SIGNAL_OKAY;
        }
        if (state != gpsState) {
            synchronized (workoutRecorderListeners) {
                for (WorkoutRecorderListener listener : workoutRecorderListeners) {
                    listener.onGPSStateChanged(gpsState, state);
                }
            }
            gpsState = state;
        }
    }

    private void resume() {
        Log.i("Recorder", "Resume");
        state = RecordingState.RUNNING;
        lastResume = System.currentTimeMillis();
        if (lastPause != 0) {
            pauseTime += System.currentTimeMillis() - lastPause;
        }
    }

    private void pause() {
        if (state == RecordingState.RUNNING) {
            Log.i("Recorder", "Pause");
            state = RecordingState.PAUSED;
            time += System.currentTimeMillis() - lastResume;
            lastPause = System.currentTimeMillis();
        }
    }

    public void stop() {
        if (state == RecordingState.PAUSED) {
            resume();
        }
        pause();
        workout.end = System.currentTimeMillis();
        workout.duration = time;
        workout.pauseDuration = pauseTime;
        state = RecordingState.STOPPED;
        Instance.getInstance(context).locationChangeListeners.remove(this);
        Log.i("Recorder", "Stop with " + getSampleCount() + " Samples");
    }

    public void save() {
        if (state != RecordingState.STOPPED) {
            throw new IllegalStateException("Cannot save recording, recorder was not stopped. state = " + state);
        }
        Log.i("Recorder", "Save");
        synchronized (samples) {
            //new WorkoutSaver(context, workout, samples).saveWorkout();
            workoutSaver.finalizeWorkout();
        }
        saved = true;
    }

    public boolean isSaved() {
        return saved;
    }

    public int getSampleCount() {
        synchronized (samples) {
            return samples.size();
        }
    }

    @Override
    public void onLocationChange(Location location) {
        lastFix = location;
        if (isActive()) {
            double distance = 0;
            if (getSampleCount() > 0) {
                // Checks whether the minimum distance to last sample was reached
                // and if the time difference to the last sample is too small
                synchronized (samples) {
                    WorkoutSample lastSample = samples.get(samples.size() - 1);
                    distance = LocationListener.locationToLatLong(location).sphericalDistance(new LatLong(lastSample.lat, lastSample.lon));
                    long timediff = lastSample.absoluteTime - location.getTime();
                    if (distance < workout.getWorkoutType().minDistance && timediff < 500) {
                        return;
                    }
                }
            }
            lastSampleTime = System.currentTimeMillis();
            if (state == RecordingState.RUNNING && location.getTime() > workout.start) {
                this.distance += distance;
                addToSamples(location);
            }
        }
    }

    private void addToSamples(Location location) {
        WorkoutSample sample = new WorkoutSample();
        sample.lat = location.getLatitude();
        sample.lon = location.getLongitude();
        sample.elevation = location.getAltitude();
        sample.speed = location.getSpeed();
        sample.relativeTime = location.getTime() - workout.start - pauseTime;
        sample.absoluteTime = location.getTime();
        if (Instance.getInstance(context).pressureAvailable) {
            sample.tmpPressure = Instance.getInstance(context).lastPressure;
        } else {
            sample.tmpPressure = -1;
        }
        synchronized (samples) {
            if(workoutSaver == null){
                throw new RuntimeException("Missing WorkoutSaver for Recorder");
            }
            workoutSaver.addSample(sample); // already persist to db
            samples.add(sample); // add to recorder list
        }
    }

    private WorkoutSample getLastSample() {
        synchronized (samples) {
            if (samples.size() > 0) {
                return samples.get(samples.size() - 1);
            } else {
                return null;
            }
        }
    }

    public void setUsedIntervalSet(IntervalSet set) {
        workout.intervalSetUsedId = set.id;
    }

    public int getDistanceInMeters() {
        return (int) distance;
    }

    private int maxCalories = 0;

    public int getCalories() {
        workout.avgSpeed = getAvgSpeed();
        workout.duration = getDuration();
        int calories = CalorieCalculator.calculateCalories(workout, Instance.getInstance(context).userPreferences.getUserWeight());
        if (calories > maxCalories) {
            maxCalories = calories;
        }
        return maxCalories;
    }

    public int getAscent() {
        double ascent = 0;
        synchronized (samples) {
            if (samples.size() == 0) {
                return 0;
            }
            double lastElevation = -1;
            for (WorkoutSample sample : samples) {
                double elevation = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, sample.tmpPressure);
                if (lastElevation == -1) lastElevation = elevation;
                elevation = (elevation + lastElevation * 9) / 10; // Slow floating average
                if (elevation > lastElevation) {
                    ascent += elevation - lastElevation;
                }
                lastElevation = elevation;
            }
        }
        System.out.println(ascent);
        return (int) ascent;
    }

    // in m/s
    public double getAvgSpeed() {
        return distance / (double) (getDuration() / 1000);
    }

    // in m/s
    public double getAvgSpeedTotal() {
        return distance / (double) (getTimeSinceStart() / 1000);
    }

    public double getCurrentSpeed() {
        WorkoutSample lastSample = getLastSample();
        if (lastSample != null) {
            return lastSample.speed;
        } else {
            return 0;
        }
    }

    public long getTimeSinceStart() {
        if (workout.start != 0) {
            return System.currentTimeMillis() - workout.start;
        } else {
            return 0;
        }
    }

    public long getPauseDuration() {
        if (state == RecordingState.PAUSED) {
            return pauseTime + (System.currentTimeMillis() - lastPause);
        } else {
            return pauseTime;
        }
    }

    public long getDuration() {
        if (state == RecordingState.RUNNING) {
            return time + (System.currentTimeMillis() - lastResume);
        } else {
            return time;
        }
    }

    public void setComment(String comment) {
        workout.comment = comment;
    }

    public boolean isPaused() {
        return state == RecordingState.PAUSED;
    }

    public RecordingState getState() {
        return state;
    }

    public void setIntervalList(List<Interval> intervalList) {
        this.intervalList = intervalList;
    }

    public List<Interval> getIntervalList() {
        return intervalList;
    }

    public void discard() {
        workoutSaver.discardWorkout();
    }

    public void addWorkoutListener(WorkoutRecorderListener listener) {
        synchronized (workoutRecorderListeners) {
            if (!workoutRecorderListeners.contains(listener)) {
                workoutRecorderListeners.add(listener);
            }
        }
    }

    public void removeWorkoutListener(WorkoutRecorderListener listener) {
        synchronized (workoutRecorderListeners) {
            workoutRecorderListeners.remove(listener);
        }
    }

    public enum RecordingState {
        IDLE, RUNNING, PAUSED, STOPPED
    }

    public enum GpsState {
        SIGNAL_LOST(Color.RED),
        SIGNAL_OKAY(Color.GREEN),
        SIGNAL_BAD(Color.YELLOW);

        public final int color;

        GpsState(int color) {
            this.color = color;
        }
    }

    public interface WorkoutRecorderListener {
        void onGPSStateChanged(GpsState oldState, GpsState state);

        void onAutoStop();
    }

}
