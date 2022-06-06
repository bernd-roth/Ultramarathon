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

package de.tadris.fitness.util;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.BaseWorkout;
import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.data.IndoorWorkout;

public class CalorieCalculator {
    private final Map<String /* WorkoutTypeId */, METFunction> metFunctions = new HashMap<>();

    static private final CalorieCalculator thisInstance = new CalorieCalculator();

    private CalorieCalculator(){
        setupMETFunctions();
    }

    public static CalorieCalculator instance() {
        return CalorieCalculator.thisInstance;
    }

    /**
     * workoutType, duration, ascent and avgSpeed of workout have to be set
     *
     * @param workout the workout
     * @return calories burned
     */
    public int calculateCalories(Context context, BaseWorkout workout) {
        double weight = Instance.getInstance(context).userPreferences.getUserWeight();
        double mins = (double) (workout.duration / 1000) / 60;
        int ascent = 0;
        if (workout instanceof GpsWorkout) {
            ascent = (int) ((GpsWorkout) workout).ascent; // 1 calorie per meter
        }
        return (int) (mins * (getMET(context, workout) * 3.5 * weight) / 200) + ascent;
    }

    /**
     * calorie calculation based on @link { https://sites.google.com/site/compendiumofphysicalactivities/Activity-Categories }
     * <p>
     * How do we get to this calculation from the values of the compendium of physical activities?
     * Easy: we have given some points like
     * - 5kmh -> 4 MET or
     * - 8kmh -> 6 MET.
     * Using regression the function "MET = f(speedInKmh)" can be created.
     * That's what you can see below for different activities.
     * <p>
     * workoutType and avgSpeed of workout have to be set
     *
     * @return MET
     */
    private double getMET(Context context, BaseWorkout workout) {
        double speedInKmh = 0;

        if (workout instanceof GpsWorkout) {
            speedInKmh = ((GpsWorkout) workout).avgSpeed * 3.6;
        } else if (workout instanceof IndoorWorkout) {
            IndoorWorkout indoorWorkout = (IndoorWorkout) workout;
            if (indoorWorkout.hasEstimatedDistance()) {
                speedInKmh = indoorWorkout.estimateSpeed(context) * 3.6;
            }
        }

        if (metFunctions.containsKey(workout.workoutTypeId)){
            return Objects.requireNonNull(metFunctions.get(workout.workoutTypeId)).getMET(speedInKmh);
        }

        // Fallback
        switch (workout.workoutTypeId) {
            case "running":
            case "walking":
            case "hiking":
            case "treadmill":
                return Math.max(3, speedInKmh * 0.97);
            case "cycling":
                return Math.max(3.5, 0.00818 * Math.pow(speedInKmh, 2) + 0.1925 * speedInKmh + 1.13);
            case "inline_skating":
                return Math.max(3, 0.6747 * speedInKmh - 2.1893);
            case "skateboarding":
                return Math.max(4, 0.43 * speedInKmh + 0.89);
            case "rowing":
                return Math.max(2.5, 0.18 * Math.pow(speedInKmh, 2) - 1.375 * speedInKmh + 5.2);
            default:
                return workout.getWorkoutType(context).MET;
        }
    }

    /**
     * Simple tuple to store Speed and MET information
     */
    private static class SpeedToMET {
        final public double avgSpeedMph;
        final public double avgMET;

        SpeedToMET(final double avgSpeedMph, final double avgMET) {
            this.avgSpeedMph = avgSpeedMph;
            this.avgMET = avgMET;
        }
    }

    /**
     * Class which represents a function to get MET based on speed. This class will use linear
     * regression to get a suitable function. This might not be helpful for all sports.
     *
     * hint: upon construction it'll calculate the function. Make sure to avoid instantiating.
     */
    private static class METFunction {
        final private double slope;
        final private double yOffset;

        private METFunction(final SpeedToMET[] lookup) {
            if (lookup.length <= 0){
                slope = 0.0;
                yOffset = 0.0;
                return;
            }

            double sumX = 0.0;
            double sumY = 0.0;
            double sumProductXY = 0.0;
            double sumSquareX = 0.0;
            for(final SpeedToMET speedToMET : lookup){
               sumX += speedToMET.avgSpeedMph;
               sumY += speedToMET.avgMET;
               sumProductXY += speedToMET.avgSpeedMph * speedToMET.avgMET;
               sumSquareX += speedToMET.avgSpeedMph * speedToMET.avgSpeedMph;
            }

            final double arithmeticAvgX = sumX / lookup.length;
            final double arithmeticAvgY = sumY / lookup.length;
            final double covarianceXY = ((1.0/lookup.length) * sumProductXY) - (arithmeticAvgX*arithmeticAvgY);
            double varianceX = ((1.0/ lookup.length) * sumSquareX) - (arithmeticAvgX * arithmeticAvgX);

            this.slope = covarianceXY / varianceX;
            this.yOffset = arithmeticAvgY - (slope * arithmeticAvgX);
        }

        public double getMET(final double speedInKmh){
            if(slope == 0.0 || yOffset == 0.0){
                assert(false);
                return 0.0;
            }

            final double speedInMph = speedInKmh * 0.621371;
            return (speedInMph * slope) + yOffset;
        }
    }

    private void setupMETFunctions() {
        final SpeedToMET[] lookupWalking = new SpeedToMET[]{
                new SpeedToMET(2.0, 2.8),
                new SpeedToMET(2.5, 3.0),
                new SpeedToMET(3.0, 3.5),
                new SpeedToMET(3.5, 4.3),
                new SpeedToMET(4.0, 5.0),
                new SpeedToMET(5.0, 8.3),
        };
        metFunctions.put("walking", new METFunction(lookupWalking));

        final SpeedToMET[] lookupRunning= new SpeedToMET[]{
                new SpeedToMET(4.0, 6.0),
                new SpeedToMET(5.0, 8.3),
                new SpeedToMET(5.2, 9.0),
                new SpeedToMET(6.0, 9.8),
                new SpeedToMET(6.7, 10.5),
                new SpeedToMET(7.0, 11.0),
                new SpeedToMET(7.5, 11.8),
                new SpeedToMET(8.0, 11.8),
                new SpeedToMET(8.6, 12.3),
                new SpeedToMET(9.0, 12.8),
                new SpeedToMET(10.0, 14.5),
                new SpeedToMET(11.0, 16.0),
                new SpeedToMET(12.0, 19.0),
                new SpeedToMET(13.0, 19.8),
                new SpeedToMET(14.0, 23.0),
        };
        metFunctions.put("running", new METFunction(lookupRunning));

        final SpeedToMET[] lookupCycling = new SpeedToMET[]{
                new SpeedToMET(5.5, 3.5),
                new SpeedToMET(9.4, 5.8),
                new SpeedToMET(11.0, 6.8),
                new SpeedToMET(13.0, 8.0),
                new SpeedToMET(15.0, 10.0),
                new SpeedToMET(17.5, 12.0),
        };
        metFunctions.put("cycling", new METFunction(lookupCycling));

    }
}
