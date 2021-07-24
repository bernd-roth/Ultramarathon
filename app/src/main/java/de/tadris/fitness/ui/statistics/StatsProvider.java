package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataProvider;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.Icon;
import de.tadris.fitness.util.WorkoutProperty;

public class StatsProvider {

    private static final int MINUTES_LIMIT = 2;

    Context ctx;
    StatsDataProvider dataProvider;

    public StatsProvider(Context ctx) {
        this.ctx = ctx;
        dataProvider = new StatsDataProvider(ctx);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarData numberOfActivities(StatsDataTypes.TimeSpan timeSpan) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Integer> numberOfWorkouts = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WorkoutProperty.LENGTH,
                WorkoutType.getAllTypes(ctx),
                timeSpan);

        // Count number of workouts of specific WorkoutType in a specific time span
        for (StatsDataTypes.DataPoint dataPoint : workouts) {
            numberOfWorkouts.put(dataPoint.workoutType,
                    numberOfWorkouts.getOrDefault(dataPoint.workoutType, 0) + 1);
        }

        for (Map.Entry<WorkoutType, Integer> entry : numberOfWorkouts.entrySet()) {

            barEntries.add(new BarEntry(
                    (float)barNumber,
                    entry.getValue(),
                    AppCompatResources.getDrawable(ctx, Icon.getIcon(entry.getKey().icon))));

            barNumber++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, ctx.getString(R.string.numberOfWorkouts));
        return new BarData(barDataSet);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarData totalDistances(StatsDataTypes.TimeSpan timeSpan) {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.LENGTH;

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Float> distances = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WORKOUT_PROPERTY, WorkoutType.getAllTypes(ctx));

        for (StatsDataTypes.DataPoint dataPoint : workouts) {
            distances.put(dataPoint.workoutType,
                    distances.getOrDefault(dataPoint.workoutType, (float)0) + (float)dataPoint.value);
        }

        //Retrieve data and add to the list
        for (Map.Entry<WorkoutType, Float> entry : distances.entrySet()) {

            barEntries.add(new BarEntry(
                    (float)barNumber,
                    entry.getValue(),
                    AppCompatResources.getDrawable(ctx, Icon.getIcon(entry.getKey().icon))));

            barNumber++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, WORKOUT_PROPERTY.getStringRepresentation(ctx));
        return new BarData(barDataSet);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarData totalDurations(StatsDataTypes.TimeSpan timeSpan)
    {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.DURATION;

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Long> durations = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WORKOUT_PROPERTY, WorkoutType.getAllTypes(ctx));

        for (StatsDataTypes.DataPoint dataPoint : workouts)
        {
            durations.put(dataPoint.workoutType,
                    durations.getOrDefault(dataPoint.workoutType, (long)0) + (long)dataPoint.value);
        }

        // Check if the durations should be displayed in minutes or hours
        boolean displayHours = TimeUnit.MILLISECONDS.toMinutes(Collections.max(durations.values())) > MINUTES_LIMIT;

        for (Map.Entry<WorkoutType, Long> entry : durations.entrySet())
        {
            long duration;
            if (displayHours) {
                duration = TimeUnit.MILLISECONDS.toHours(entry.getValue());
            } else {
                duration = TimeUnit.MILLISECONDS.toMinutes(entry.getValue());
            }

            barEntries.add(new BarEntry(
                    (float)barNumber,
                    (float)duration,
                    AppCompatResources.getDrawable(ctx, Icon.getIcon(entry.getKey().icon))));

            barNumber++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, WORKOUT_PROPERTY.getStringRepresentation(ctx));
        return new BarData(barDataSet);
    }

    public CandleData getPaceData(AggregationSpan span, WorkoutType workoutType) {

        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.AVG_PACE;

        ArrayList<StatsDataTypes.DataPoint> data = dataProvider.getData(WORKOUT_PROPERTY,
                WorkoutType.getAllTypes(ctx));

        long oldestWorkoutTime = Collections.min(data, StatsDataTypes.DataPoint.timeComparator).time;
        long newestWorkoutTime = Collections.max(data, StatsDataTypes.DataPoint.timeComparator).time;

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(oldestWorkoutTime);

        span.applyToCalendar(calendar);

        ArrayList<CandleEntry> candleEntries = new ArrayList<>();

        while (calendar.getTimeInMillis() < newestWorkoutTime) {
            ArrayList<StatsDataTypes.DataPoint> intervalData = dataProvider.getData(WORKOUT_PROPERTY,
                    WorkoutType.getAllTypes(ctx),
                    new StatsDataTypes.TimeSpan(calendar.getTimeInMillis(), calendar.getTimeInMillis() + span.spanInterval));

            // Calculate average value
            float mean = 0;
            for (StatsDataTypes.DataPoint dataPoint : intervalData) {
                mean += dataPoint.value;
            }
            mean /= intervalData.size();

            if (intervalData.size() > 0) {
                float min = (float) Collections.min(intervalData, StatsDataTypes.DataPoint.valueComparator).value;
                float max = (float) Collections.max(intervalData, StatsDataTypes.DataPoint.valueComparator).value;

                candleEntries.add(new CandleEntry((float) (calendar.getTimeInMillis()/1000000000.0), max, min, mean, mean));
            }

            // increment
            int days = (int) TimeUnit.MILLISECONDS.toDays(span.spanInterval);
            calendar.add(Calendar.DAY_OF_YEAR, days);
            calendar.add(Calendar.MILLISECOND, (int) (span.spanInterval - TimeUnit.DAYS.toMillis(days)));
        }

        CandleDataSet candleDataSet = new CandleDataSet(candleEntries, WORKOUT_PROPERTY.getStringRepresentation(ctx));
        candleDataSet.setShadowColor(Color.GRAY);
        candleDataSet.setShadowWidth(2f);
        candleDataSet.setNeutralColor(ContextCompat.getColor(ctx, R.color.colorPrimary));

        return new CandleData(candleDataSet);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public CandleData speedCombinedChart(WorkoutProperty workoutProperty , StatsDataTypes.TimeSpan timeSpan, WorkoutType workoutType) {
        ArrayList<CandleEntry> candleEntries = new ArrayList<>();

        HashMap<Long, ArrayList<Float>> dataPoints = new HashMap<>();

//        ArrayList<Float> timePoints = new ArrayList<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(workoutProperty, (List<WorkoutType>) workoutType);

        for (StatsDataTypes.DataPoint dataPoint : workouts) {
            if (timeSpan.contains(dataPoint.time)) {
                ArrayList<Float> pointsList = dataPoints.getOrDefault(dataPoint.time, new ArrayList<>());
                pointsList.add((float) dataPoint.value);
                dataPoints.put(dataPoint.time, pointsList);
            }
        }

        for (Map.Entry<Long, ArrayList<Float>> entry : dataPoints.entrySet()) {

            candleEntries.add(new CandleEntry(
                    (float)entry.getKey(), Collections.max(entry.getValue()),
                    Collections.min(entry.getValue()), calculateAverage(entry.getValue()),
                    calculateAverage(entry.getValue())));
        }

        CandleDataSet candleDataSet = new CandleDataSet(candleEntries, workoutProperty.getStringRepresentation(ctx));
        return new CandleData(candleDataSet);
    }

    private float calculateAverage(ArrayList<Float> marks) {
        Float sum = 0f;
        if(!marks.isEmpty()) {
            for (Float mark : marks) {
                sum += mark;
            }
            return sum.floatValue() / marks.size();
        }
        return sum;
    }

}
