package de.tadris.fitness.data;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.aggregation.WorkoutTypeFilter;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.charts.formatter.SpeedFormatter;
import de.tadris.fitness.util.charts.formatter.TimeFormatter;
import de.tadris.fitness.util.exceptions.NoDataException;

import static java.lang.Math.min;

public class StatsProvider {

    public enum StatsType {
        NUMBER_OF_ACTIVITIES(0),
        TOTAL_DISTANCES(1),
        TOTAL_DURATIONS(2),
        PACE_CANDLE_DATA(3),
        SPEED_CANDLE_DATA(4),
        DISTANCE_CANDLE_DATA(5),
        DISTANCE_SUM_DATA(6),
        DURATION_CANDLE_DATA(7),
        DURATION_SUM_DATA(8),
        PAUSE_DURATION_CANDLE_DATA(9),
        PAUSE_DURATION_SUM_DATA(10);

        int index;

        StatsType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static StatsType getByIndex(int index) {
            return StatsType.values()[index];
        }

        public DataSet getDataSet(Context ctx, StatsDataTypes.TimeSpan timeSpan, @Nullable WorkoutType workoutType) throws NoDataException {
            return null;
        }
    }

    static TypedValue stats_time_factor;
    Context ctx;
    StatsDataProvider dataProvider;

    public StatsProvider(Context ctx) {
        this.ctx = ctx;
        dataProvider = new StatsDataProvider(ctx);
        stats_time_factor = new TypedValue();
        ctx.getResources().getValue(R.dimen.stats_time_factor, stats_time_factor, true);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarDataSet numberOfActivities(StatsDataTypes.TimeSpan timeSpan) throws NoDataException {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Integer> numberOfWorkouts = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WorkoutProperty.LENGTH,
                WorkoutTypeManager.getInstance().getAllTypes(ctx),
                timeSpan);

        if (workouts.isEmpty()) {
            throw new NoDataException();
        }

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

        BarDataSet dataSet = DataSetStyles.applyDefaultBarStyle(ctx, new BarDataSet(barEntries,
                String.valueOf(StatsType.NUMBER_OF_ACTIVITIES.getIndex())));
        dataSet.setValueFormatter(new DefaultValueFormatter(0));
        return dataSet;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarDataSet totalDistances(StatsDataTypes.TimeSpan timeSpan) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.LENGTH;

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Float> distances = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WORKOUT_PROPERTY, WorkoutTypeManager.getInstance().getAllTypes(ctx), timeSpan);

        if (workouts.isEmpty()) {
            throw new NoDataException();
        }

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
        BarDataSet dataSet = DataSetStyles.applyDefaultBarStyle(ctx,
                new BarDataSet(barEntries, String.valueOf(StatsType.TOTAL_DISTANCES.getIndex())));
        dataSet.setValueFormatter(new DefaultValueFormatter(1));
        return dataSet;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarDataSet totalDurations(StatsDataTypes.TimeSpan timeSpan) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.DURATION;

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Long> durations = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WORKOUT_PROPERTY, WorkoutTypeManager.getInstance().getAllTypes(ctx), timeSpan);

        if (workouts.isEmpty()) {
            throw new NoDataException();
        }

        for (StatsDataTypes.DataPoint dataPoint : workouts) {
            durations.put(dataPoint.workoutType,
                    durations.getOrDefault(dataPoint.workoutType, (long) 0) + (long) dataPoint.value);
        }

        // Sort numberOfWorkouts map
        ArrayList<Map.Entry<WorkoutType, Long>> sortedDurations = new ArrayList<>(durations.entrySet());
        Collections.sort(sortedDurations, (first, second) -> second.getValue().compareTo(first.getValue()));


        for (Map.Entry<WorkoutType, Long> entry : sortedDurations) {
            long duration = entry.getValue();

            barEntries.add(new BarEntry(
                    (float) barNumber,
                    (float) duration,
                    entry.getKey()));

            barNumber++;
        }

        BarDataSet dataSet = DataSetStyles.applyDefaultBarStyle(ctx,
                new BarDataSet(barEntries, String.valueOf(StatsType.TOTAL_DURATIONS.getIndex())));
        dataSet.setValueFormatter(getCorrectTimeFormatter(TimeUnit.MILLISECONDS, (long)dataSet.getYMax()));
        return dataSet;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarDataSet createHistogramData(List<Double> values, int bins, String label) {
        Double[] weights = new Double[values.size()];
        Arrays.fill(weights, 1);
        return createWeightedHistogramData(values, Arrays.asList(weights), bins, label);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarDataSet createWeightedHistogramData(List<Double> values, List<Double> weights, int bins, String label) {
        Collections.sort(values);
        double min = values.get(0);
        double max = values.get(values.size()-1);
        double binWidth = (max-min)/bins;
        double[] histogram = new double[bins];
        int binIndex=0;

        for(int i=0; i<values.size(); i++)
        {
            if(values.get(i) <= min((binIndex+1)*binWidth+min, max))
                histogram[binIndex] += weights.get(i);
            else {
                binIndex++;
            }
        }

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for(int i=0; i<bins; i++)
        {
            barEntries.add(new BarEntry((float)(min+binWidth*i), (float) histogram[i]));
        }

        return DataSetStyles.applyDefaultBarStyle(ctx,
                new BarDataSet(barEntries, label));
    }

    public CandleDataSet getPaceCandleData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.AVG_PACE;

        CandleDataSet candleDataSet = new CandleDataSet(getCombinedCandleData(span, workoutTypes, WORKOUT_PROPERTY),
                String.valueOf(StatsType.PACE_CANDLE_DATA.getIndex()));
        CandleDataSet dataSet = DataSetStyles.applyDefaultCandleStyle(ctx, candleDataSet);
        dataSet.setValueFormatter(getCorrectTimeFormatter(TimeUnit.MINUTES, (long)dataSet.getYMax()));
        return DataSetStyles.applyDefaultCandleStyle(ctx, candleDataSet);
    }

    public LineDataSet getPaceLineData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        return convertCandleToMeanLineData(getPaceCandleData(span, workoutTypes));
    }


    public CandleDataSet getSpeedCandleData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.AVG_SPEED;

        CandleDataSet candleDataSet = new CandleDataSet(getCombinedCandleData(span, workoutTypes, WORKOUT_PROPERTY),
                String.valueOf(StatsType.SPEED_CANDLE_DATA.getIndex()));
        candleDataSet.setValueFormatter(new SpeedFormatter(Instance.getInstance(ctx).distanceUnitUtils));
        return DataSetStyles.applyDefaultCandleStyle(ctx, candleDataSet);
    }

    public LineDataSet getSpeedLineData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        return convertCandleToMeanLineData(getSpeedCandleData(span, workoutTypes));
    }


    public CandleDataSet getDistanceCandleData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.LENGTH;

        ArrayList<CandleEntry> candleEntries = getCombinedCandleData(span, workoutTypes, WORKOUT_PROPERTY);

        // Display distance in kilometers
        for (CandleEntry entry : candleEntries) {
            entry.setHigh(entry.getHigh() / 1000);
            entry.setLow(entry.getLow() / 1000);
            entry.setOpen(entry.getOpen() / 1000);
            entry.setClose(entry.getClose() / 1000);
        }

        return DataSetStyles.applyDefaultCandleStyle(ctx, new CandleDataSet(candleEntries,
                String.valueOf(StatsType.DISTANCE_CANDLE_DATA.getIndex())));
    }

    public LineDataSet getDistanceLineData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        return convertCandleToMeanLineData(getDistanceCandleData(span, workoutTypes));
    }

    public BarDataSet getDistanceSumData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.LENGTH;

        ArrayList<BarEntry> barEntries = getCombinedSumData(span, workoutTypes, WORKOUT_PROPERTY);

        for (BarEntry entry : barEntries) {
            entry.setY(entry.getY() / 1000);
        }

        return DataSetStyles.applyDefaultBarStyle(ctx, new BarDataSet(barEntries,
                String.valueOf(StatsType.DISTANCE_SUM_DATA.getIndex())));
    }


    public CandleDataSet getDurationCandleData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.DURATION;

        ArrayList<CandleEntry> candleEntries = getCombinedCandleData(span, workoutTypes, WORKOUT_PROPERTY);

        CandleDataSet dataSet = DataSetStyles.applyDefaultCandleStyle(ctx, new CandleDataSet(candleEntries,
                String.valueOf(StatsType.DURATION_CANDLE_DATA.getIndex())));
        dataSet.setValueFormatter(getCorrectTimeFormatter(TimeUnit.MILLISECONDS, (long)dataSet.getYMax()));
        return dataSet;
    }

    public LineDataSet getDurationLineData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        return convertCandleToMeanLineData(getDurationCandleData(span, workoutTypes));
    }

    public BarDataSet getDurationSumData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.DURATION;

        ArrayList<BarEntry> barEntries = getCombinedSumData(span, workoutTypes, WORKOUT_PROPERTY);

        BarDataSet dataSet = DataSetStyles.applyDefaultBarStyle(ctx, new BarDataSet(barEntries,
                String.valueOf(StatsType.DURATION_SUM_DATA.getIndex())));
        dataSet.setValueFormatter(getCorrectTimeFormatter(TimeUnit.MILLISECONDS, (long) dataSet.getYMax()));
        return dataSet;
    }


    public CandleDataSet getPauseDurationCandleData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.PAUSE_DURATION;

        ArrayList<CandleEntry> candleEntries = getCombinedCandleData(span, workoutTypes, WORKOUT_PROPERTY);

        CandleDataSet dataSet = DataSetStyles.applyDefaultCandleStyle(ctx, new CandleDataSet(candleEntries,
                String.valueOf(StatsType.PAUSE_DURATION_CANDLE_DATA.getIndex())));
        dataSet.setValueFormatter(getCorrectTimeFormatter(TimeUnit.MILLISECONDS, (long)dataSet.getYMax()));
        return dataSet;
    }

    public LineDataSet getPauseDurationLineData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        return convertCandleToMeanLineData(getPauseDurationCandleData(span, workoutTypes));
    }

    public BarDataSet getPauseDurationSumData(AggregationSpan span, List<WorkoutType> workoutTypes) throws NoDataException {
        final WorkoutProperty WORKOUT_PROPERTY = WorkoutProperty.PAUSE_DURATION;

        ArrayList<BarEntry> barEntries = getCombinedSumData(span, workoutTypes, WORKOUT_PROPERTY);

        BarDataSet dataSet = DataSetStyles.applyDefaultBarStyle(ctx, new BarDataSet(barEntries,
                String.valueOf(StatsType.PAUSE_DURATION_SUM_DATA.getIndex())));
        dataSet.setValueFormatter(getCorrectTimeFormatter(TimeUnit.MILLISECONDS, (long)dataSet.getYMax()));
        return dataSet;
    }

    private static TimeFormatter getCorrectTimeFormatter(TimeUnit unit, long maxTime)
    {
        if(unit.toMillis(maxTime) > TimeUnit.HOURS.toMillis(1))
            return new TimeFormatter(unit, false, true, true);
        else
            return new TimeFormatter(unit, true, true, false);
    }

    private ArrayList<StatsDataTypes.DataPoint> findDataPointsInAggregationSpan(ArrayList<StatsDataTypes.DataPoint> data, Calendar startTime, AggregationSpan span) {
        // Retrieve the workoutProperty for all workouts in the specific time span
        StatsDataTypes.TimeSpan timeSpan = new StatsDataTypes.TimeSpan(startTime.getTimeInMillis(), span.getAggregationEnd(startTime).getTimeInMillis());
        ArrayList<StatsDataTypes.DataPoint> intervalData = new ArrayList<>();

        // Create list of data points belonging to the same time span
        Iterator<StatsDataTypes.DataPoint> dataPointIterator = data.iterator();
        while (dataPointIterator.hasNext()) {
            StatsDataTypes.DataPoint dataPoint = dataPointIterator.next();
            if (timeSpan.contains(dataPoint.time)) {
                intervalData.add(dataPoint);
                data.remove(dataPointIterator);
            }
        }
        return intervalData;
    }


    private ArrayList<CandleEntry> getCombinedCandleData(AggregationSpan span, List<WorkoutType> workoutTypes, WorkoutProperty workoutProperty) throws NoDataException {
        ArrayList<StatsDataTypes.DataPoint> data = dataProvider.getData(workoutProperty,
                workoutTypes);

        if (data.isEmpty()) {
            throw new NoDataException();
        }

        ArrayList<CandleEntry> candleEntries = new ArrayList<>();

        if (span == AggregationSpan.SINGLE) {
            // No aggregation
            for (StatsDataTypes.DataPoint dataPoint : data) {
                float value = (float) dataPoint.value;
                candleEntries.add(new CandleEntry((float) dataPoint.time / stats_time_factor.getFloat(), value, value, value, value, dataPoint));
            }
        } else {
            // Find start and end time of workouts
            long oldestWorkoutTime = Collections.min(data, StatsDataTypes.DataPoint.timeComparator).time;
            long newestWorkoutTime = Collections.max(data, StatsDataTypes.DataPoint.timeComparator).time;

            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(oldestWorkoutTime);

            span.setCalendarToAggregationStart(calendar);


            // Iterate all time spans from first workout time to last workout time
            while (calendar.getTimeInMillis() < newestWorkoutTime) {
                ArrayList<StatsDataTypes.DataPoint> intervalData = findDataPointsInAggregationSpan(data, calendar, span);

                // Calculate min, max and average of the data of the span and store in the candle list
                if (intervalData.size() > 0) {
                    float min = (float) Collections.min(intervalData, StatsDataTypes.DataPoint.valueComparator).value;
                    float max = (float) Collections.max(intervalData, StatsDataTypes.DataPoint.valueComparator).value;
                    float mean = calculateValueAverage(intervalData);
                    candleEntries.add(new CandleEntry((float) calendar.getTimeInMillis() / stats_time_factor.getFloat(), max, min, mean, mean));
                }

                // Increment time span
                if (span != AggregationSpan.ALL) {
                    calendar.add(span.calendarField, 1);
                } else  {
                    calendar.setTimeInMillis(Long.MAX_VALUE);
                }
            }
        }

        return candleEntries;
    }

    public ArrayList<BarEntry> getCombinedSumData(AggregationSpan span, List<WorkoutType> workoutTypes, WorkoutProperty workoutProperty) throws NoDataException {
        ArrayList<StatsDataTypes.DataPoint> data = dataProvider.getData(workoutProperty,
                workoutTypes);

        if (data.isEmpty()) {
            throw new NoDataException();
        }

        ArrayList<BarEntry> barEntries = new ArrayList<>();

        if (span == AggregationSpan.SINGLE) {
            // No aggregation
            for (StatsDataTypes.DataPoint dataPoint : data) {
                float value = (float) dataPoint.value;
                barEntries.add(new BarEntry((float) dataPoint.time / stats_time_factor.getFloat(), value));
            }
        } else {
            // Find start and end time of workouts
            long oldestWorkoutTime = Collections.min(data, StatsDataTypes.DataPoint.timeComparator).time;
            long newestWorkoutTime = Collections.max(data, StatsDataTypes.DataPoint.timeComparator).time;

            // Find start time of aggregation span
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(oldestWorkoutTime);
            span.setCalendarToAggregationStart(calendar);

            // Iterate all time spans from first workout time to last workout time
            while (calendar.getTimeInMillis() < newestWorkoutTime) {
                ArrayList<StatsDataTypes.DataPoint> intervalData = findDataPointsInAggregationSpan(data, calendar, span);

                // Calculate the sum of the data from the span and store in the bar list
                if (intervalData.size() > 0) {
                    float sum = calculateValueSum(intervalData);
                    barEntries.add(new BarEntry((float) calendar.getTimeInMillis() / stats_time_factor.getFloat(), sum));
                }

                // Increment time span
                if (span != AggregationSpan.ALL) {
                    calendar.add(span.calendarField, 1);
                } else  {
                    calendar.setTimeInMillis(Long.MAX_VALUE);
                }
            }
        }

        return barEntries;
    }



    public static LineDataSet convertCandleToMeanLineData(CandleDataSet candleDataSet) {
        ArrayList<Entry> lineData = new ArrayList<>();

        for (CandleEntry entry : candleDataSet.getValues()) {
            lineData.add(new Entry(entry.getX(), entry.getClose()));
        }

        LineDataSet dataSet = new LineDataSet(lineData, candleDataSet.getLabel());
        dataSet.setValueFormatter(candleDataSet.getValueFormatter());
        return dataSet;
    }

    private float calculateValueAverage(ArrayList<StatsDataTypes.DataPoint> marks) {
        float average = 0f;
        if (!marks.isEmpty()) {
            average = calculateValueSum(marks) / marks.size();
        }
        return average;
    }

    private float calculateValueSum(ArrayList<StatsDataTypes.DataPoint> marks) {
        float sum = 0f;
        for (StatsDataTypes.DataPoint mark : marks) {
            sum += mark.value;
        }
        return sum;
    }

    public void setXLimits(Chart chart) {
        try {
            chart.getXAxis().setAxisMinimum(dataProvider.getFirstData(
                    WorkoutProperty.TOP_SPEED, WorkoutTypeManager.getInstance().getAllTypes(ctx)).time);
            chart.getXAxis().setAxisMaximum(dataProvider.getLastData(
                    WorkoutProperty.TOP_SPEED, WorkoutTypeManager.getInstance().getAllTypes(ctx)).time);
        } catch (NoDataException e) {
            e.printStackTrace();
        }
    }

}
