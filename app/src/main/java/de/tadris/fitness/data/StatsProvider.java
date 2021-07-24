package de.tadris.fitness.data;

import android.content.Context;
import android.graphics.Color;
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
                    entry.getKey()));

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
                    entry.getKey()));

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
                    entry.getKey()));

            barNumber++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, WORKOUT_PROPERTY.getStringRepresentation(ctx));
        return new BarData(barDataSet);
    }

    public CandleData getPaceData(AggregationSpan span, WorkoutType workoutType) {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.AVG_PACE;

        CandleDataSet candleDataSet = new CandleDataSet(getCombinedData(span, workoutType, WORKOUT_PROPERTY),
                WORKOUT_PROPERTY.getStringRepresentation(ctx));

        applyDefaultCandleStyle(candleDataSet);

        return new CandleData(candleDataSet);
    }

    public CandleData getSpeedData(AggregationSpan span, WorkoutType workoutType) {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.AVG_SPEED;

        CandleDataSet candleDataSet = new CandleDataSet(getCombinedData(span, workoutType, WORKOUT_PROPERTY),
                WORKOUT_PROPERTY.getStringRepresentation(ctx));

        applyDefaultCandleStyle(candleDataSet);

        return new CandleData(candleDataSet);
    }

    private ArrayList<CandleEntry> getCombinedData(AggregationSpan span, WorkoutType workoutType, WorkoutProperty workoutProperty) {

        ArrayList<WorkoutType> workoutTypes = new ArrayList<WorkoutType>() {
            {add(workoutType);}
        };

        // Find start and end time of workout
        ArrayList<StatsDataTypes.DataPoint> data = dataProvider.getData(workoutProperty,
                workoutTypes);

        long oldestWorkoutTime = Collections.min(data, StatsDataTypes.DataPoint.timeComparator).time;
        long newestWorkoutTime = Collections.max(data, StatsDataTypes.DataPoint.timeComparator).time;

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(oldestWorkoutTime);

        span.applyToCalendar(calendar);

        ArrayList<CandleEntry> candleEntries = new ArrayList<>();

        // Iterate all time spans from first workout time to last workout time
        while (calendar.getTimeInMillis() < newestWorkoutTime) {
            // Retrieve the workoutProperty for all workouts in the specific time span
            ArrayList<StatsDataTypes.DataPoint> intervalData = dataProvider.getData(workoutProperty,
                    workoutTypes,
                    new StatsDataTypes.TimeSpan(calendar.getTimeInMillis(), calendar.getTimeInMillis() + span.spanInterval));

            if (intervalData.size() > 0) {
                float min = (float) Collections.min(intervalData, StatsDataTypes.DataPoint.valueComparator).value;
                float max = (float) Collections.max(intervalData, StatsDataTypes.DataPoint.valueComparator).value;
                float mean = calculateValueAverage(intervalData);

                candleEntries.add(new CandleEntry((float) (calendar.getTimeInMillis()/1000000000.0), max, min, mean, mean));
            }

            // increment by time span
            int days = (int) TimeUnit.MILLISECONDS.toDays(span.spanInterval);
            calendar.add(Calendar.DAY_OF_YEAR, days);
            calendar.add(Calendar.MILLISECOND, (int) (span.spanInterval - TimeUnit.DAYS.toMillis(days)));
        }

        return candleEntries;
    }

    private CandleDataSet applyDefaultCandleStyle(CandleDataSet candleDataSet) {
        candleDataSet.setShadowColor(Color.GRAY);
        candleDataSet.setShadowWidth(2f);
        candleDataSet.setNeutralColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
        return candleDataSet;
    }

    private float calculateValueAverage(ArrayList<StatsDataTypes.DataPoint> marks) {
        float sum = 0f;
        if(!marks.isEmpty()) {
            for (StatsDataTypes.DataPoint mark : marks) {
                sum += mark.value;
            }
            return sum / marks.size();
        }
        return sum;
    }

}
