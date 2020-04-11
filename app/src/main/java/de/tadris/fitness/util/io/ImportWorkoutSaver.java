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

package de.tadris.fitness.util.io;

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

class ImportWorkoutSaver {

    private final Context context;
    private final Workout workout;
    private final List<WorkoutSample> samples;
    private final AppDatabase db;

    public ImportWorkoutSaver(Context context, Workout workout, List<WorkoutSample> samples) {
        this.context = context;
        this.workout = workout;
        this.samples = samples;
        db= Instance.getInstance(context).db;
    }

    public void saveWorkout(){
        setIds();
        setSimpleValues();
        setTopSpeed();

        setAscentAndDescent();

        setCalories();

        storeInDatabase();
    }

    private void setIds(){
        workout.id= System.currentTimeMillis();
        int i= 0;
        for(WorkoutSample sample : samples) {
            i++;
            sample.id = workout.id + i;
            sample.workoutId = workout.id;
        }
    }

    private void setSimpleValues(){
        double length= 0;
        for(int i= 1; i < samples.size(); i++){
            double sampleLength= samples.get(i - 1).toLatLong().sphericalDistance(samples.get(i).toLatLong());
            length+= sampleLength;
        }
        workout.length= (int)length;
        workout.avgSpeed= ((double) workout.length) / ((double) workout.duration / 1000);
        workout.avgPace= ((double)workout.duration / 1000 / 60) / ((double) workout.length / 1000);
    }

    private void setTopSpeed(){
        double topSpeed= 0;
        for(WorkoutSample sample : samples){
            if(sample.speed > topSpeed){
                topSpeed= sample.speed;
            }
        }
        workout.topSpeed= topSpeed;
    }

    private void setAscentAndDescent(){
        workout.ascent = 0;
        workout.descent = 0;

        // Now sum up the ascent/descent
        for(int i= 0; i < samples.size(); i++) {
            WorkoutSample sample = samples.get(i);
            if(i >= 1){
                WorkoutSample lastSample= samples.get(i-1);
                double diff= sample.elevation - lastSample.elevation;
                if(diff > 0){
                    // If this sample is higher than the last one, add difference to ascent
                    workout.ascent += diff;
                }else{
                    // If this sample is lower than the last one, add difference to descent
                    workout.descent += Math.abs(diff);
                }
            }
        }

    }

    private void setCalories() {
        // Ascent has to be set previously
        workout.calorie = CalorieCalculator.calculateCalories(workout, Instance.getInstance(context).userPreferences.getUserWeight());
    }

    private void storeInDatabase(){
        db.workoutDao().insertWorkoutAndSamples(workout, samples.toArray(new WorkoutSample[0]));
    }
}
