package de.tadris.fitness.util;

import android.content.Context;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.util.charts.formatter.FractionedDateFormatter;
import de.tadris.fitness.util.charts.formatter.SpeedFormatter;
import de.tadris.fitness.util.charts.formatter.TimeFormatter;

public enum WorkoutProperty {
    START(0),
    END(1),
    DURATION(2),
    PAUSE_DURATION(3),
    AVG_HEART_RATE(4),
    MAX_HEART_RATE(5),
    CALORIE(6),

    // GPS Workout
    LENGTH(7),
    AVG_SPEED(8),
    TOP_SPEED(9),
    AVG_PACE(10),
    ASCENT(11),
    DESCENT(12),

    // Indoor Workout
    AVG_FREQUENCY(13),
    MAX_FREQUENCY(14),
    MAX_INTENSITY(15),
    AVG_INTENSITY(16);

    private int id;

    WorkoutProperty(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static WorkoutProperty getById(int id)
    {
        return WorkoutProperty.values()[id];
    }

    private static final List<WorkoutProperty> summable = Arrays.asList(DURATION, PAUSE_DURATION, CALORIE, LENGTH, ASCENT, DESCENT);
    public boolean summable() {return summable.contains(this);}

    public boolean isBaseProperty() { return id <=CALORIE.getId(); }
    public boolean isGPSProperty() { return CALORIE.getId() < id && id <= DESCENT.getId(); }
    public boolean isIndoorProperty() { return DESCENT.getId() < id && id <=CALORIE.getId(); }

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

    public ValueFormatter getValueFormatter(Context ctx, float value) {
        switch (this) {
            case START:
            case END:
                //return new FractionedDateFormatter(ctx, span);
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
                return new DefaultValueFormatter(1);

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
    public String getUnit(Context ctx, float value)
    {
        switch (this) {
            case START:
            case END:
            case DURATION:
            case PAUSE_DURATION:
                return ((TimeFormatter)getValueFormatter(ctx, value)).getUnit(ctx);
            case AVG_PACE:
                return Instance.getInstance(ctx).distanceUnitUtils.getPaceUnit();

            case AVG_SPEED:
            case TOP_SPEED:
                return Instance.getInstance(ctx).distanceUnitUtils.getDistanceUnitSystem().getSpeedUnit();

            case ASCENT:
            case DESCENT:
                return Instance.getInstance(ctx).distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit();
            case LENGTH:
                return Instance.getInstance(ctx).distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit();

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
