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
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.IntervalSet;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.CalorieCalculator;

public class WorkoutRecorder implements LocationListener.LocationChangeListener {

    private static final int PAUSE_TIME= 10000;

    /**
     * Time after which the workout is stopped and saved automatically because there is no activity anymore
     */
    private static final int AUTO_STOP_TIMEOUT= 1000*60*60*20; // 20 minutes

    private final Context context;
    private final Workout workout;
    private RecordingState state;
    private final List<WorkoutSample> samples= new ArrayList<>();
    private long time= 0;
    private long pauseTime= 0;
    private long lastResume;
    private long lastPause= 0;
    private long lastSampleTime= 0;
    private double distance= 0;

    private static final double SIGNAL_BAD_THRESHOLD = 30; // In meters
    private static final int SIGNAL_LOST_THRESHOLD= 10000; // In milliseconds
    private Location lastFix= null;
    private final WorkoutRecorderListener workoutRecorderListener;
    private GpsState gpsState= GpsState.SIGNAL_LOST;

    public WorkoutRecorder(Context context, WorkoutType workoutType, WorkoutRecorderListener workoutRecorderListener) {
        this.context= context;
        this.state= RecordingState.IDLE;
        this.workoutRecorderListener = workoutRecorderListener;

        this.workout= new Workout();
        workout.edited = false;

        // Default values
        this.workout.comment= "";
        this.workout.intervalSetIncludesPauses = Instance.getInstance(context).userPreferences.intervalsIncludePauses();

        this.workout.setWorkoutType(workoutType);

        init();
    }

    private void init() {
        Instance.getInstance(context).locationChangeListeners.add(this);
        startWatchdog();
    }

    public void start(){
        if(state == RecordingState.IDLE){
            Log.i("Recorder", "Start");
            workout.start= System.currentTimeMillis();
            resume();
        }else if(state == RecordingState.PAUSED){
            resume();
        }else if(state != RecordingState.RUNNING){
            throw new IllegalStateException("Cannot start or resume recording. state = " + state);
        }
    }

    public boolean isActive(){
        return state == RecordingState.IDLE || state == RecordingState.RUNNING || state == RecordingState.PAUSED;
    }

    public boolean isResumed() {
        return state == RecordingState.RUNNING;
    }

    private void startWatchdog(){
        new Thread(() -> {
            try {
                while (isActive()){
                    checkSignalState();
                    synchronized (samples){
                        if(samples.size() > 2){
                            WorkoutSample lastSample= samples.get(samples.size()-1);
                            long timeDiff= System.currentTimeMillis() - lastSampleTime;
                            if(timeDiff > AUTO_STOP_TIMEOUT){
                                if(isActive()){
                                    stop();
                                    save();
                                    workoutRecorderListener.onAutoStop();
                                }
                            }else if(timeDiff > PAUSE_TIME){
                                if (state == RecordingState.RUNNING && gpsState != GpsState.SIGNAL_LOST) {
                                    pause();
                                }
                            }else{
                                if(state == RecordingState.PAUSED){
                                    resume();
                                }
                            }
                        }
                    }
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "WorkoutWatchdog").start();
    }

    private void checkSignalState(){
        if(lastFix==null){
            return;
        }
        GpsState state;
        if(System.currentTimeMillis() - lastFix.getTime() > SIGNAL_LOST_THRESHOLD){
            state= GpsState.SIGNAL_LOST;
        }else if(lastFix.getAccuracy() > SIGNAL_BAD_THRESHOLD){
            state= GpsState.SIGNAL_BAD;
        }else{
            state= GpsState.SIGNAL_OKAY;
        }
        if(state != gpsState){
            workoutRecorderListener.onGPSStateChanged(gpsState, state);
            gpsState= state;
        }
    }

    private void resume(){
        Log.i("Recorder", "Resume");
        state= RecordingState.RUNNING;
        lastResume= System.currentTimeMillis();
        if(lastPause != 0){
            pauseTime+= System.currentTimeMillis() - lastPause;
        }
    }

    private void pause() {
        if(state == RecordingState.RUNNING){
            Log.i("Recorder", "Pause");
            state= RecordingState.PAUSED;
            time+= System.currentTimeMillis() - lastResume;
            lastPause= System.currentTimeMillis();
        }
    }

    public void stop(){
        Log.i("Recorder", "Stop");
        if(state == RecordingState.PAUSED){
            resume();
        }
        pause();
        workout.end= System.currentTimeMillis();
        workout.duration= time;
        workout.pauseDuration= pauseTime;
        state= RecordingState.STOPPED;
        Instance.getInstance(context).locationChangeListeners.remove(this);
    }

    public void save(){
        if(state != RecordingState.STOPPED){
            throw new IllegalStateException("Cannot save recording, recorder was not stopped. state = " + state);
        }
        Log.i("Recorder", "Save");
        synchronized (samples){
            new WorkoutSaver(context, workout, samples).saveWorkout();
        }
    }

    public int getSampleCount(){
        synchronized (samples){
            return samples.size();
        }
    }

    @Override
    public void onLocationChange(Location location) {
        lastFix= location;
        if(isActive()){
            double distance= 0;
            if(getSampleCount() > 0){
                // Checks whether the minimum distance to last sample was reached
                // and if the time difference to the last sample is too small
                synchronized (samples){
                    WorkoutSample lastSample= samples.get(samples.size() - 1);
                    distance= LocationListener.locationToLatLong(location).sphericalDistance(new LatLong(lastSample.lat, lastSample.lon));
                    long timediff= lastSample.absoluteTime - location.getTime();
                    if (distance < workout.getWorkoutType().minDistance && timediff < 500) {
                        return;
                    }
                }
            }
            lastSampleTime= System.currentTimeMillis();
            if(state == RecordingState.RUNNING && location.getTime() > workout.start){
                this.distance+= distance;
                addToSamples(location);
            }
        }
    }

    private void addToSamples(Location location){
        WorkoutSample sample= new WorkoutSample();
        sample.lat= location.getLatitude();
        sample.lon= location.getLongitude();
        sample.elevation= location.getAltitude();
        sample.speed= location.getSpeed();
        sample.relativeTime= location.getTime() - workout.start - pauseTime;
        sample.absoluteTime= location.getTime();
        if(Instance.getInstance(context).pressureAvailable){
            sample.tmpPressure= Instance.getInstance(context).lastPressure;
        }else{
            sample.tmpPressure= -1;
        }
        synchronized (samples){
            samples.add(sample);
        }
    }

    private WorkoutSample getLastSample() {
        synchronized (samples) {
            return samples.get(samples.size() - 1);
        }
    }

    public void setUsedIntervalSet(IntervalSet set) {
        workout.intervalSetUsedId = set.id;
    }

    public int getDistanceInMeters() {
        return (int)distance;
    }

    private int maxCalories= 0;
    public int getCalories(){
        workout.avgSpeed= getAvgSpeed();
        workout.duration= getDuration();
        int calories= CalorieCalculator.calculateCalories(workout, Instance.getInstance(context).userPreferences.getUserWeight());
        if(calories > maxCalories){
            maxCalories= calories;
        }
        return maxCalories;
    }

    public int getAscent() {
        double lastElevation = samples.get(0).elevation;
        double ascent = 0;
        synchronized (samples) {
            for (WorkoutSample sample : samples) {
                double elevation = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, sample.tmpPressure);
                elevation = (elevation + lastElevation * 6) / 7; // Slow floating average 1/7
                if (elevation > lastElevation) {
                    ascent += elevation - lastElevation;
                }
                lastElevation = sample.elevation;
            }
        }
        return (int) ascent;
    }

    /**
     *
     * @return avgSpeed in m/s
     */
    public double getAvgSpeed(){
        return distance / (double)(getDuration() / 1000);
    }

    public double getCurrentSpeed() {
        return getLastSample().speed;
    }

    public long getTimeSinceStart() {
        if (workout.start != 0) {
            return System.currentTimeMillis() - workout.start;
        } else {
            return 0;
        }
    }

    public long getPauseDuration(){
        if(state == RecordingState.PAUSED){
            return pauseTime + (System.currentTimeMillis() - lastPause);
        }else{
            return pauseTime;
        }
    }

    public long getDuration(){
        if(state == RecordingState.RUNNING){
            return time + (System.currentTimeMillis() - lastResume);
        }else{
            return time;
        }
    }

    public void setComment(String comment){
        workout.comment= comment;
    }

    public boolean isPaused(){
        return state == RecordingState.PAUSED;
    }

    public RecordingState getState() {
        return state;
    }


    public enum RecordingState {
        IDLE, RUNNING, PAUSED, STOPPED
    }

    public enum GpsState{
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
