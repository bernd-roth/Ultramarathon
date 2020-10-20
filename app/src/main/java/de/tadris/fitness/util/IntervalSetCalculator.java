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

package de.tadris.fitness.util;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.data.Interval;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutData;
import de.tadris.fitness.data.WorkoutSample;

public class IntervalSetCalculator {

    public static List<Long> getTimesFromWorkout(WorkoutData data, Interval[] intervals) {
        List<Long> result = new ArrayList<>();
        Workout workout = data.getWorkout();
        List<WorkoutSample> samples = data.getSamples();

        int index = 0;
        if (workout.intervalSetIncludesPauses) {
            long time = 0;
            long lastTime = samples.get(0).absoluteTime;
            for (WorkoutSample sample : samples) {
                if (index >= intervals.length) {
                    index = 0;
                }
                Interval currentInterval = intervals[index];
                time += sample.absoluteTime - lastTime;
                if (time > currentInterval.delayMillis) {
                    time = 0;
                    index++;
                    result.add(sample.relativeTime);
                }
                lastTime = sample.absoluteTime;
            }
        } else {
            long time = 0;
            while (time < workout.duration) {
                if (index >= intervals.length) {
                    index = 0;
                }
                Interval interval = intervals[index];

                result.add(time);

                time += interval.delayMillis;
                index++;
            }
        }
        return result;
    }

}