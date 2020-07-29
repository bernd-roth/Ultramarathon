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
import android.hardware.SensorManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.AppDatabase;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.util.AltitudeCorrection;
import de.tadris.fitness.util.CalorieCalculator;

public class WorkoutSaver {

    private final Context context;
    protected final Workout workout;
    protected final List<WorkoutSample> samples;
    private final AppDatabase db;

    public WorkoutSaver(Context context, Workout workout, List<WorkoutSample> samples) {
        this.context = context;
        this.workout = workout;
        this.samples = samples;
        this.db = Instance.getInstance(context).db;
    }

    public void finalizeWorkout() {
        clearSamplesWithSameTime(true);

        calculateData();

        updateWorkoutAndSamples();
    }

    public void discardWorkout() {
        deleteWorkoutAndSamples();
    }

    public synchronized void addSample(WorkoutSample sample) {
        sample.id = this.workout.id + this.samples.size();
        sample.workoutId = this.workout.id;
        db.workoutDao().insertSample(sample);
    }

    public void saveWorkout() {
        setIds();

        clearSamplesWithSameTime(false);
        calculateData();

        storeInDatabase();
    }

    protected void setIds() {
        workout.id = System.currentTimeMillis();
        int i = 0;
        for (WorkoutSample sample : samples) {
            i++;
            sample.id = workout.id + i;
            sample.workoutId = workout.id;
        }
    }

    private void calculateData() {
        setSimpleValues();
        setTopSpeed();

        setElevation();
        setRoundedSampleElevation();
        setAscentAndDescent();

        setCalories();
    }

    private void clearSamplesWithSameTime(boolean delete) {
        for (int i = samples.size() - 2; i >= 0; i--) {
            WorkoutSample sample = samples.get(i);
            WorkoutSample lastSample = samples.get(i + 1);
            if (sample.absoluteTime == lastSample.absoluteTime) {
                samples.remove(lastSample);
                if (delete) {
                    db.workoutDao().deleteSample(lastSample); // delete sample also from DB
                }
                Log.i("WorkoutManager", "Removed samples at " + sample.absoluteTime + " rel: " + sample.relativeTime + "; " + lastSample.relativeTime);
            }
        }
    }

    protected void setSimpleValues() {
        double length = 0;
        for (int i = 1; i < samples.size(); i++) {
            double sampleLength = samples.get(i - 1).toLatLong().sphericalDistance(samples.get(i).toLatLong());
            length += sampleLength;
        }
        workout.length = (int) length;
        workout.avgSpeed = ((double) workout.length) / ((double) workout.duration / 1000);
        workout.avgPace = ((double) workout.duration / 1000 / 60) / ((double) workout.length / 1000);
    }

    protected void setTopSpeed() {
        double topSpeed = 0;
        for (WorkoutSample sample : samples) {
            if (sample.speed > topSpeed) {
                topSpeed = sample.speed;
            }
        }
        workout.topSpeed = topSpeed;
    }

    private void setElevation() {
        setCorrectedElevation();
        setPressureElevation();
    }

    private void setCorrectedElevation() {
        // Please see the AltitudeCorrection.java for the reason of this
        try {
            int lat = (int) Math.round(samples.get(0).lat);
            int lon = (int) Math.round(samples.get(0).lon);
            AltitudeCorrection correction = new AltitudeCorrection(context, lat, lon);
            for (WorkoutSample sample : samples) {
                sample.elevation = correction.getHeightOverSeaLevel(sample.elevation);
            }
        } catch (IOException e) {
            // If we can't read the file, we cannot correct the values
            e.printStackTrace();
        }
    }

    private void setPressureElevation() {
        boolean pressureDataAvailable = samples.get(0).pressure != -1;

        if (!pressureDataAvailable) {
            // Because pressure data isn't available we just use the use GPS elevation
            // in WorkoutSample.elevation which was already set
            return;
        }

        double avgElevation = getAverageElevation();
        double avgPressure = getAveragePressure();

        for (int i = 0; i < samples.size(); i++) {
            WorkoutSample sample = samples.get(i);

            // Altitude Difference to Average Elevation in meters
            float altitude_difference =
                    SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, sample.pressure) -
                            SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, (float) avgPressure);
            sample.elevation = avgElevation + altitude_difference;
        }
    }

    private double getAverageElevation() {
        return getAverageElevation(samples);
    }

    private double getAverageElevation(List<WorkoutSample> samples) {
        double elevationSum = 0; // Sum of elevation
        for (WorkoutSample sample : samples) {
            elevationSum += sample.elevation;
        }

        return elevationSum / samples.size();
    }

    private double getAveragePressure() {
        double pressureSum = 0;
        for (WorkoutSample sample : samples) {
            pressureSum += sample.pressure;
        }
        return pressureSum / samples.size();
    }

    private void setRoundedSampleElevation() {
        // Should only be done on newly recorded samples
        roundSampleElevation();
        for (WorkoutSample sample : samples) {
            sample.elevation = sample.tmpElevation;
        }
    }

    private void roundSampleElevation() {
        int range = 7;
        for (int i = 0; i < samples.size(); i++) {
            int minIndex = Math.max(i - range, 0);
            int maxIndex = Math.min(i + range, samples.size() - 1);
            samples.get(i).tmpElevation = getAverageElevation(samples.subList(minIndex, maxIndex));
        }
    }

    protected void setAscentAndDescent() {
        workout.ascent = 0f;
        workout.descent = 0f;

        // Eliminate pressure noise
        roundSampleElevation();

        // Now sum up the ascent/descent
        if (samples.size() > 1) {
            WorkoutSample prevSample = samples.get(0);
            for (int i = 1; i < samples.size(); i++) {
                WorkoutSample sample = samples.get(i);
                // Use Rounded Elevations
                double diff = sample.elevation - prevSample.elevation;
                if (Double.isNaN(diff)) {
                    Log.e("WorkoutSaver", "ElevationDiff is NaN fallback to 0");
                    diff = 0d;
                }
                if (diff > 0) {
                    // If this sample is higher than the last one, add difference to ascent
                    workout.ascent += diff;
                } else {
                    // If this sample is lower than the last one, add difference to descent
                    workout.descent += Math.abs(diff);
                }
                prevSample = sample;
            }
        }
    }

    protected void setCalories() {
        // Ascent has to be set previously
        workout.calorie = CalorieCalculator.calculateCalories(workout, Instance.getInstance(context).userPreferences.getUserWeight());
    }

    protected void storeInDatabase() {
        db.workoutDao().insertWorkoutAndSamples(workout, samples.toArray(new WorkoutSample[0]));
    }

    protected void storeWorkoutInDatabase() {
        db.workoutDao().insertWorkout(workout);
    }

    protected void updateWorkoutAndSamples() {
        updateWorkoutInDatabase();
        updateSamples();
    }

    protected void updateSamples() {
        db.workoutDao().updateSamples(samples.toArray(new WorkoutSample[0]));
    }

    protected void updateWorkoutInDatabase() {
        db.workoutDao().updateWorkout(workout);
    }

    protected void deleteWorkoutAndSamples() {
        db.workoutDao().deleteWorkoutAndSamples(workout, samples.toArray(new WorkoutSample[0]));
    }
}
