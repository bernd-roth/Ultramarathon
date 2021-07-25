package de.tadris.fitness.data;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.aggregation.WorkoutTypeFilter;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.exceptions.NoDataException;

public class StatsProvider {

    private static final int MINUTES_LIMIT = 2;

    Context ctx;
    StatsDataProvider dataProvider;

    public StatsProvider(Context ctx) {
        this.ctx = ctx;
        dataProvider = new StatsDataProvider(ctx);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarDataSet numberOfActivities(StatsDataTypes.TimeSpan timeSpan) {
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

        // Sort numberOfWorkouts map
        ArrayList<Map.Entry<WorkoutType, Integer>> sortedNumberOfWorkouts = new ArrayList<>(numberOfWorkouts.entrySet());
        Collections.sort(sortedNumberOfWorkouts, (first, second) -> second.getValue().compareTo(first.getValue()));

        // Create Bar Chart
        for (Map.Entry<WorkoutType, Integer> entry : sortedNumberOfWorkouts) {
            barEntries.add(new BarEntry(
                    (float) barNumber,
                    entry.getValue(),
                    entry.getKey()));

            barNumber++;
        }

        return DataSetStyles.applyDefaultBarStyle(ctx, new BarDataSet(barEntries,
                ctx.getString(R.string.numberOfWorkouts)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarDataSet totalDistances(StatsDataTypes.TimeSpan timeSpan) {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.LENGTH;

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Float> distances = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WORKOUT_PROPERTY, WorkoutType.getAllTypes(ctx));

        for (StatsDataTypes.DataPoint dataPoint : workouts) {
            distances.put(dataPoint.workoutType,
                    distances.getOrDefault(dataPoint.workoutType, (float) 0) + (float) dataPoint.value / 1000);
        }

        // Sort numberOfWorkouts map
        ArrayList<Map.Entry<WorkoutType, Float>> sortedDistances = new ArrayList<>(distances.entrySet());
        Collections.sort(sortedDistances, (first, second) -> second.getValue().compareTo(first.getValue()));

        //Retrieve data and add to the list
        for (Map.Entry<WorkoutType, Float> entry : sortedDistances) {

            barEntries.add(new BarEntry(
                    (float) barNumber,
                    entry.getValue(),
                    entry.getKey()));

            barNumber++;
        }

        return DataSetStyles.applyDefaultBarStyle(ctx,
                new BarDataSet(barEntries, WORKOUT_PROPERTY.getStringRepresentation(ctx)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarDataSet totalDurations(StatsDataTypes.TimeSpan timeSpan) {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.DURATION;

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Long> durations = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WORKOUT_PROPERTY, WorkoutType.getAllTypes(ctx));

        for (StatsDataTypes.DataPoint dataPoint : workouts) {
            durations.put(dataPoint.workoutType,
                    durations.getOrDefault(dataPoint.workoutType, (long) 0) + (long) dataPoint.value);
        }

        // Sort numberOfWorkouts map
        ArrayList<Map.Entry<WorkoutType, Long>> sortedDurations = new ArrayList<>(durations.entrySet());
        Collections.sort(sortedDurations, (first, second) -> second.getValue().compareTo(first.getValue()));

        // Check if the durations should be displayed in minutes or hours
        boolean displayHours = TimeUnit.MILLISECONDS.toMinutes(Collections.max(durations.values())) > MINUTES_LIMIT;

        for (Map.Entry<WorkoutType, Long> entry : sortedDurations) {
            long duration;
            if (displayHours) {
                duration = TimeUnit.MILLISECONDS.toHours(entry.getValue());
            } else {
                duration = TimeUnit.MILLISECONDS.toMinutes(entry.getValue());
            }

            barEntries.add(new BarEntry(
                    (float) barNumber,
                    (float) duration,
                    entry.getKey()));

            barNumber++;
        }

        return DataSetStyles.applyDefaultBarStyle(ctx,
                new BarDataSet(barEntries, WORKOUT_PROPERTY.getStringRepresentation(ctx)));
    }

    public CandleDataSet getPaceCandleData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.AVG_PACE;

        CandleDataSet candleDataSet = new CandleDataSet(getCombinedData(span, workoutType, WORKOUT_PROPERTY),
                WORKOUT_PROPERTY.getStringRepresentation(ctx));

        return DataSetStyles.applyDefaultCandleStyle(ctx, candleDataSet);
    }

    public LineDataSet getPaceLineData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        return convertCandleToMeanLineData(getPaceCandleData(span, workoutType));
    }


    public CandleDataSet getSpeedCandleData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.AVG_SPEED;

        CandleDataSet candleDataSet = new CandleDataSet(getCombinedData(span, workoutType, WORKOUT_PROPERTY),
                WORKOUT_PROPERTY.getStringRepresentation(ctx));

        return DataSetStyles.applyDefaultCandleStyle(ctx, candleDataSet);
    }

    public LineDataSet getSpeedLineData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        return convertCandleToMeanLineData(getSpeedCandleData(span, workoutType));
    }


    public CandleDataSet getDistanceCandleData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.LENGTH;

        ArrayList<CandleEntry> candleEntries = getCombinedData(span, workoutType, WORKOUT_PROPERTY);

        // Display distance in kilometers
        for (CandleEntry entry : candleEntries) {
            entry.setHigh(entry.getHigh() / 1000);
            entry.setLow(entry.getLow() / 1000);
            entry.setOpen(entry.getOpen() / 1000);
            entry.setClose(entry.getClose() / 1000);
        }

        return DataSetStyles.applyDefaultCandleStyle(ctx, new CandleDataSet(candleEntries,
                WORKOUT_PROPERTY.getStringRepresentation(ctx)));
    }

    public LineDataSet getDistanceLineData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        return convertCandleToMeanLineData(getDistanceCandleData(span, workoutType));
    }


    public CandleDataSet getDurationCandleData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.DURATION;

        ArrayList<CandleEntry> candleEntries = getCombinedData(span, workoutType, WORKOUT_PROPERTY);

        // Display durations in minutes
        for (CandleEntry entry : candleEntries) {
            entry.setHigh(TimeUnit.MILLISECONDS.toMinutes((long) entry.getHigh()));
            entry.setLow(TimeUnit.MILLISECONDS.toMinutes((long) entry.getLow()));
            entry.setOpen(TimeUnit.MILLISECONDS.toMinutes((long) entry.getOpen()));
            entry.setClose(TimeUnit.MILLISECONDS.toMinutes((long) entry.getClose()));
        }

        return DataSetStyles.applyDefaultCandleStyle(ctx, new CandleDataSet(candleEntries,
                WORKOUT_PROPERTY.getStringRepresentation(ctx)));
    }

    public LineDataSet getDurationLineData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        return convertCandleToMeanLineData(getDurationCandleData(span, workoutType));
    }


    public CandleDataSet getPauseDurationCandleData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.PAUSE_DURATION;

        ArrayList<CandleEntry> candleEntries = getCombinedData(span, workoutType, WORKOUT_PROPERTY);

        for (CandleEntry entry : candleEntries) {
            entry.setHigh(TimeUnit.MILLISECONDS.toMinutes((long) entry.getHigh()));
            entry.setLow(TimeUnit.MILLISECONDS.toMinutes((long) entry.getLow()));
            entry.setOpen(TimeUnit.MILLISECONDS.toMinutes((long) entry.getOpen()));
            entry.setClose(TimeUnit.MILLISECONDS.toMinutes((long) entry.getClose()));
        }

        return DataSetStyles.applyDefaultCandleStyle(ctx, new CandleDataSet(candleEntries,
                WORKOUT_PROPERTY.getStringRepresentation(ctx)));
    }

    public LineDataSet getPauseDurationLineData(AggregationSpan span, WorkoutType workoutType) throws NoDataException {
        return convertCandleToMeanLineData(getPauseDurationCandleData(span, workoutType));
    }


    private ArrayList<CandleEntry> getCombinedData(AggregationSpan span, WorkoutType workoutType, WorkoutProperty workoutProperty) throws NoDataException {

        ArrayList<WorkoutType> workoutTypes = new ArrayList<WorkoutType>();

        // Convert workout type to list (_all) type should be converted to a list with all Workout types
        if (workoutType.id.equals(WorkoutTypeFilter.ID_ALL)) {
            workoutTypes = (ArrayList<WorkoutType>) WorkoutType.getAllTypes(ctx);
        } else {
            workoutTypes.add(workoutType);
        }

        // Find start and end time of workout
        ArrayList<StatsDataTypes.DataPoint> data = dataProvider.getData(workoutProperty,
                workoutTypes);

        if (data.isEmpty()) {
            throw new NoDataException(workoutType);
        }

        long oldestWorkoutTime = Collections.min(data, StatsDataTypes.DataPoint.timeComparator).time;
        long newestWorkoutTime = Collections.max(data, StatsDataTypes.DataPoint.timeComparator).time;

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(oldestWorkoutTime);

        span.applyToCalendar(calendar);

        ArrayList<CandleEntry> candleEntries = new ArrayList<>();

        // Iterate all time spans from first workout time to last workout time
        while (calendar.getTimeInMillis() < newestWorkoutTime) {
            // Retrieve the workoutProperty for all workouts in the specific time span
            StatsDataTypes.TimeSpan timeSpan = new StatsDataTypes.TimeSpan(calendar.getTimeInMillis(), calendar.getTimeInMillis() + span.spanInterval);
            ArrayList<StatsDataTypes.DataPoint> intervalData = new ArrayList<>();

            Iterator<StatsDataTypes.DataPoint> dataPointIterator = data.iterator();
            while (dataPointIterator.hasNext()) {
                StatsDataTypes.DataPoint dataPoint = dataPointIterator.next();
                if (timeSpan.contains(dataPoint.time)) {
                    intervalData.add(dataPoint);
                    data.remove(dataPointIterator);
                }
            }

            if (intervalData.size() > 0) {
                float min = (float) Collections.min(intervalData, StatsDataTypes.DataPoint.valueComparator).value;
                float max = (float) Collections.max(intervalData, StatsDataTypes.DataPoint.valueComparator).value;
                float mean = calculateValueAverage(intervalData);

                candleEntries.add(new CandleEntry((float) (calendar.getTimeInMillis() / 1000000000.0), max, min, mean, mean));
            }

            // increment by time span
            int days = (int) TimeUnit.MILLISECONDS.toDays(span.spanInterval);
            calendar.add(Calendar.DAY_OF_YEAR, days);
            calendar.add(Calendar.MILLISECOND, (int) (span.spanInterval - TimeUnit.DAYS.toMillis(days)));
        }

        return candleEntries;
    }

    public static LineDataSet convertCandleToMeanLineData(CandleDataSet candleDataSet) {
        ArrayList<Entry> lineData = new ArrayList<>();

        for (CandleEntry entry : candleDataSet.getValues()) {
            lineData.add(new Entry(entry.getX(), entry.getClose()));
        }

        return new LineDataSet(lineData, candleDataSet.getLabel());
    }

    private float calculateValueAverage(ArrayList<StatsDataTypes.DataPoint> marks) {
        float sum = 0f;
        if (!marks.isEmpty()) {
            for (StatsDataTypes.DataPoint mark : marks) {
                sum += mark.value;
            }
            return sum / marks.size();
        }
        return sum;
    }

}
