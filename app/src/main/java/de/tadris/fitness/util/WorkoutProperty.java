/*
 * Copyright (c) 2022 Jannis Scheibe <jannis@tadris.de>
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

import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.charts.formatter.DistanceFormatter;
import de.tadris.fitness.util.charts.formatter.FractionedDateFormatter;
import de.tadris.fitness.util.charts.formatter.SpeedFormatter;
import de.tadris.fitness.util.charts.formatter.TimeFormatter;

public enum WorkoutProperty {
    NUMBER(0, WorkoutPropertyType.BASE),
    START(1, WorkoutPropertyType.BASE),
    END(2, WorkoutPropertyType.BASE),
    DURATION(3, WorkoutPropertyType.BASE),
    PAUSE_DURATION(4, WorkoutPropertyType.BASE),
    AVG_HEART_RATE(5, WorkoutPropertyType.BASE),
    MAX_HEART_RATE(6, WorkoutPropertyType.BASE),
    CALORIE(7, WorkoutPropertyType.BASE),

    // GPS Workout
    LENGTH(8, WorkoutPropertyType.GPS),
    AVG_SPEED(9, WorkoutPropertyType.GPS),
    TOP_SPEED(10, WorkoutPropertyType.GPS),
    AVG_PACE(11, WorkoutPropertyType.GPS),
    ASCENT(12, WorkoutPropertyType.GPS),
    DESCENT(13, WorkoutPropertyType.GPS),

    // Indoor Workout
    AVG_FREQUENCY(14, WorkoutPropertyType.INDOOR),
    MAX_FREQUENCY(15, WorkoutPropertyType.INDOOR),
    MAX_INTENSITY(16, WorkoutPropertyType.INDOOR),
    AVG_INTENSITY(17, WorkoutPropertyType.INDOOR);

    private final int id;
    private final WorkoutPropertyType type;

    WorkoutProperty(int id, WorkoutPropertyType type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public WorkoutPropertyType getType() {
        return type;
    }

    public static WorkoutProperty getById(int id) {
        return WorkoutProperty.values()[id];
    }

    private static final List<WorkoutProperty> summable = Arrays.asList(DURATION, PAUSE_DURATION, CALORIE, LENGTH, ASCENT, DESCENT);

    public boolean summable() {
        return summable.contains(this);
    }

    public String getStringRepresentation(Context ctx) {
        return getStringRepresentations(ctx).get(id);
    }

    public static List<String> getStringRepresentations(Context context) {
        List<String> criteria = new ArrayList<>();
        criteria.add(context.getString(R.string.workoutStartTime));
        criteria.add(context.getString(R.string.workoutEndTime));
        criteria.add(context.getString(R.string.workoutDuration));
        criteria.add(context.getString(R.string.workoutPauseDuration));
        criteria.add(context.getString(R.string.workoutAvgHeartRate));
        criteria.add(context.getString(R.string.workoutMaxHeartRate));
        criteria.add(context.getString(R.string.workoutBurnedEnergy));

        // GPS Workout
        criteria.add(context.getString(R.string.workoutDistance));
        criteria.add(context.getString(R.string.workoutAvgSpeedShort));
        criteria.add(context.getString(R.string.workoutTopSpeed));
        criteria.add(context.getString(R.string.workoutPace));
        criteria.add(context.getString(R.string.workoutAscent));
        criteria.add(context.getString(R.string.workoutDescent));

        // Indoor Workout
        criteria.add(context.getString(R.string.workoutFrequency));
        criteria.add(context.getString(R.string.workoutMaxFrequency));
        criteria.add(context.getString(R.string.workoutIntensity));
        criteria.add(context.getString(R.string.workoutMaxIntensity));
        return criteria;
    }

    public String getFormattedValue(Context ctx, float value) {
        return getFormattedValue(ctx, value, true);
    }

    public String getFormattedValue(Context ctx, float value, boolean unit) {
        String formatted = getValueFormatter(ctx, value).getFormattedValue(value);
        if (unit)
            formatted += " " + getUnit(ctx, value);
        return formatted;
    }

    public ValueFormatter getValueFormatter(Context ctx, float value) {
        switch (this) {
            case NUMBER:
                return new DefaultValueFormatter(0);
            case START:
            case END:
                return new FractionedDateFormatter(ctx, ChartStyles.statsAggregationSpan((long) value));
            case DURATION:
            case PAUSE_DURATION:
                return StatsProvider.getCorrectTimeFormatter(TimeUnit.MILLISECONDS, (long) value);
            case AVG_PACE:
                return StatsProvider.getCorrectTimeFormatter(TimeUnit.MINUTES, (long) value);

            case AVG_SPEED:
            case TOP_SPEED:
                return new SpeedFormatter(Instance.getInstance(ctx).distanceUnitUtils);

            case ASCENT:
            case DESCENT:
            case LENGTH:
                return new DistanceFormatter(Instance.getInstance(ctx).distanceUnitUtils, (int)value);

            case CALORIE:
            case AVG_INTENSITY:
            case MAX_INTENSITY:

            case MAX_FREQUENCY:
            case AVG_FREQUENCY:

            case AVG_HEART_RATE:
            case MAX_HEART_RATE:
            default:
                return new DefaultValueFormatter(2);
        }
    }

    public String getUnit(Context ctx, float value) {
        switch (this) {
            case START:
            case END:
                return ctx.getString(((FractionedDateFormatter) getValueFormatter(ctx, value)).getSpan().axisLabel);
            case DURATION:
            case PAUSE_DURATION:
                return ((TimeFormatter) getValueFormatter(ctx, value)).getUnit(ctx);
            case AVG_PACE:
                return Instance.getInstance(ctx).distanceUnitUtils.getPaceUnit();

            case AVG_SPEED:
            case TOP_SPEED:
                return Instance.getInstance(ctx).distanceUnitUtils.getDistanceUnitSystem().getSpeedUnit();

            case ASCENT:
            case DESCENT:
            case LENGTH:
                if(value / Instance.getInstance(ctx).distanceUnitUtils.getDistanceUnitSystem().getMetersFromLongDistance(1) > 1)
                    return Instance.getInstance(ctx).distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit();
                else
                    return Instance.getInstance(ctx).distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit();

            case NUMBER:
            case CALORIE:
            case AVG_INTENSITY:
            case MAX_INTENSITY:

            case MAX_FREQUENCY:
            case AVG_FREQUENCY:

            case AVG_HEART_RATE:
            case MAX_HEART_RATE:
            default:
                return "";
        }
    }
}
