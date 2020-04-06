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

import org.junit.Assert;
import org.junit.Test;

import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.CalorieCalculator;

public class CalorieCalculatorTest {

    @Test
    public void testCalculation() {
        Workout workout = new Workout();
        workout.avgSpeed = 2.7d;
        workout.setWorkoutType(WorkoutType.RUNNING);
        workout.duration = 1000L * 60 * 10;
        int calorie = CalorieCalculator.calculateCalories(workout, 80);
        System.out.println("Calories: " + calorie);
        Assert.assertEquals(130, calorie, 50);
    }

}
