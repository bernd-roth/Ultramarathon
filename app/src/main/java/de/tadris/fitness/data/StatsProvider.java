package de.tadris.fitness.data;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.charts.formatter.FractionedDateFormatter;
import de.tadris.fitness.util.charts.formatter.SpeedFormatter;
import de.tadris.fitness.util.charts.formatter.TimeFormatter;
import de.tadris.fitness.util.exceptions.NoDataException;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class StatsProvider {
    public static float STATS_TIME_FACTOR;
    Context ctx;
    StatsDataProvider dataProvider;

    public StatsProvider(Context ctx) {
        this.ctx = ctx;
        dataProvider = new StatsDataProvider(ctx);
        TypedValue factor = new TypedValue();
        ctx.getResources().getValue(R.dimen.stats_time_factor, factor, true);
        STATS_TIME_FACTOR = factor.getFloat();
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

        BarDataSet dataSet = DataSetStyles.applyDefaultBarStyle(ctx, new BarDataSet(barEntries, ctx.getString(R.string.numberOfWorkouts)));
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
                    distances.getOrDefault(dataPoint.workoutType, (float) 0) + (float) dataPoint.value);
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
                new BarDataSet(barEntries, WORKOUT_PROPERTY.name()));
        dataSet.setValueFormatter(WORKOUT_PROPERTY.getValueFormatter(ctx, dataSet.getYMax()));
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
                new BarDataSet(barEntries, WORKOUT_PROPERTY.name()));
        dataSet.setValueFormatter(WORKOUT_PROPERTY.getValueFormatter(ctx, dataSet.getYMax()));
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

    public CandleDataSet getCandleData(AggregationSpan span, List<WorkoutType> workoutTypes, WorkoutProperty workoutProperty) throws NoDataException {
        ArrayList<CandleEntry> candleEntries = getCombinedCandleData(span, workoutTypes, workoutProperty);

        CandleDataSet dataSet = DataSetStyles.applyDefaultCandleStyle(ctx, new CandleDataSet(candleEntries,
                workoutProperty.getStringRepresentation(ctx)));
        dataSet.setValueFormatter(workoutProperty.getValueFormatter(ctx, dataSet.getYMax()));
        return dataSet;
    }

    public BarDataSet getSumData(AggregationSpan span, List<WorkoutType> workoutTypes, WorkoutProperty workoutProperty) throws NoDataException {
        ArrayList<BarEntry> barEntries = getCombinedSumData(span, workoutTypes, workoutProperty);
        BarDataSet dataSet = DataSetStyles.applyDefaultBarStyle(ctx, new BarDataSet(barEntries,
                workoutProperty.getStringRepresentation(ctx)));
        dataSet.setValueFormatter(workoutProperty.getValueFormatter(ctx, dataSet.getYMax()));
        return dataSet;
    }

    public static TimeFormatter getCorrectTimeFormatter(TimeUnit unit, long maxTime)
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

    public ArrayList<CandleEntry> getCombinedCandleData(AggregationSpan span, List<WorkoutType> workoutTypes, WorkoutProperty workoutProperty) throws NoDataException {
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
                candleEntries.add(new CandleEntry((float) dataPoint.time / STATS_TIME_FACTOR, value, value, value, value, dataPoint));
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
                    candleEntries.add(new CandleEntry((float) calendar.getTimeInMillis() / STATS_TIME_FACTOR, max, min, mean, mean));
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
                barEntries.add(new BarEntry((float) dataPoint.time / STATS_TIME_FACTOR, value));
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
                    barEntries.add(new BarEntry((float) calendar.getTimeInMillis() / STATS_TIME_FACTOR, sum));
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

    public List<BubbleDataSet> getExperimentalData(List<WorkoutType> workoutTypes, StatsDataTypes.TimeSpan timeSpan, AggregationSpan aggregationSpan, WorkoutProperty xAxis, WorkoutProperty yAxis, WorkoutProperty bubbleSize) throws NoDataException {
        List<BubbleDataSet> dataSets = new ArrayList<>();
        for(WorkoutType type : workoutTypes) {
            List<WorkoutType> typeList = new ArrayList<>();
            typeList.add(type);
            ArrayList<StatsDataTypes.DataPoint> xData = dataProvider.getData(xAxis,
                    typeList, timeSpan);
            ArrayList<StatsDataTypes.DataPoint> yData = dataProvider.getData(yAxis,
                    typeList, timeSpan);
            ArrayList<StatsDataTypes.DataPoint> bubbleData = dataProvider.getData(bubbleSize,
                    typeList, timeSpan);

            if (xData.isEmpty() || yData.isEmpty() || bubbleData.isEmpty()) {
                continue;
            }

            List<BubbleEntry> bubbleEntries = new ArrayList<>();
            for (int i = 0; i < xData.size(); i++) {
                bubbleEntries.add(new BubbleEntry((float) xData.get(i).value, (float) yData.get(i).value, (float) bubbleData.get(i).value));
            }
            bubbleEntries.sort((Entry a, Entry b) -> a.getX() < b.getX() ? -1 : a.getX() > b.getX() ? 1 : 0);
            BubbleDataSet set =new BubbleDataSet(bubbleEntries, type.title);
            int alpha = 255/(bubbleEntries.size());
            set.setColor(type.color, min(max(alpha, 20), 160));
            set.setValueFormatter(yAxis.getValueFormatter(ctx, set.getYMax()));
            set.setDrawValues(false);
            dataSets.add(set);
        }
        if(dataSets.size() == 0) {
            throw new NoDataException();
        }
        return dataSets;
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

    public void setAxisLimits(AxisBase axis, WorkoutProperty property) {
        try {
            axis.setAxisMinimum(dataProvider.getFirstData(
                    property, WorkoutTypeManager.getInstance().getAllTypes(ctx)).time);
            axis.setAxisMaximum(dataProvider.getLastData(
                    property, WorkoutTypeManager.getInstance().getAllTypes(ctx)).time);
        } catch (NoDataException e) {
            e.printStackTrace();
        }
    }
}
