package de.tadris.fitness.ui.statistics;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.ui.FitoTrackActivity;
import de.tadris.fitness.ui.workout.ShowGpsWorkoutActivity;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.charts.formatter.FractionedDateFormatter;
import de.tadris.fitness.util.charts.marker.DisplayValueMarker;
import de.tadris.fitness.util.exceptions.NoDataException;

public class DetailStatsActivity extends FitoTrackActivity {

    CombinedChart chart;
    float stats_time_factor;
    WorkoutTypeManager workoutTypeManager;
    ArrayList<WorkoutType> workoutTypes;
    AggregationSpan aggregationSpan;
    StatsProvider statsProvider;
    float xScale, xTrans;
    StatsProvider.StatsType statsType;
    CandleDataSet currentCandleDataSet = null;
    BarDataSet currentBarDataSet = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_detail);
        setTitle(getString(R.string.details));
        setupActionBar();

        TypedValue stats_time_factor = new TypedValue();
        this.getResources().getValue(R.dimen.stats_time_factor, stats_time_factor, true);
        this.stats_time_factor = stats_time_factor.getFloat();

        workoutTypeManager = WorkoutTypeManager.getInstance();
        statsProvider = new StatsProvider(this);
        chart = findViewById(R.id.stats_detail_chart);
        aggregationSpan = (AggregationSpan) getIntent().getSerializableExtra("aggregationSpan");
    }

    @Override
    protected void onStart() {
        super.onStart();
        int chartIndex = Integer.parseInt(getIntent().getExtras().getString("data"));
        String type = (String) getIntent().getSerializableExtra("type");
        String label = (String) getIntent().getSerializableExtra("ylabel");
        statsType = StatsProvider.StatsType.getByIndex(chartIndex);
        xScale = getIntent().getFloatExtra("xScale", 0);
        xTrans = getIntent().getFloatExtra("xTrans", 0);

        ChartStyles.defaultLineChart(chart);
        ChartStyles.setYAxisLabel(chart,label);

        WorkoutType workoutType;
        if (type.equals("_all")) {
            workoutType = new WorkoutType();
            workoutType.id = type;
        }
        else {
            workoutType = workoutTypeManager.getWorkoutTypeById(this, type);
        }
        workoutTypes = WorkoutTypeSelection.createWorkoutTypeList(this, workoutType);

        chart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
                Highlight highlight = chart.getHighlightByTouchPoint(me.getX(), me.getY());
                if (highlight != null) {
                    openWorkout(highlight.getX());
                }
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                long timeSpan = (long) ((chart.getHighestVisibleX() - chart.getLowestVisibleX()) * stats_time_factor);
                AggregationSpan oldAggregationSpan = aggregationSpan;

                if (TimeUnit.DAYS.toMillis(1095) < timeSpan) {
                    aggregationSpan = AggregationSpan.YEAR;
                } else if (TimeUnit.DAYS.toMillis(93) < timeSpan) {
                    aggregationSpan = AggregationSpan.MONTH;
                } else if (TimeUnit.DAYS.toMillis(21) < timeSpan) {
                    aggregationSpan = AggregationSpan.WEEK;
                } else {
                    aggregationSpan = AggregationSpan.SINGLE;
                }

                if (oldAggregationSpan != aggregationSpan) {
                    updateChart(workoutTypes);
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });

        updateChart(workoutTypes);

        chart.getViewPortHandler().zoom(xScale,
                chart.getViewPortHandler().getScaleY(),
                -xTrans,
                chart.getViewPortHandler().getTransY());

        animateChart(chart);
    }

    private void openWorkout(float xPosition) {
        if (currentCandleDataSet != null && aggregationSpan == AggregationSpan.SINGLE) {
            CandleEntry entry = currentCandleDataSet.getEntryForXValue(xPosition, 0);
            StatsDataTypes.DataPoint dataPoint = (StatsDataTypes.DataPoint) entry.getData();

            Intent intent = new Intent(this, dataPoint.workoutType.getRecordingType().showDetailsActivityClass);
            intent.putExtra(ShowGpsWorkoutActivity.WORKOUT_ID_EXTRA, dataPoint.workoutID);
            startActivity(intent);
        }
    }

    private void updateChart(List<WorkoutType> workoutTypes) {

        CombinedData combinedData = new CombinedData();

        // Draw candle charts
        try {
            switch (statsType) {
                case SPEED_CANDLE_DATA:
                    currentCandleDataSet = statsProvider.getSpeedCandleData(aggregationSpan, workoutTypes);
                    setTitle(this.getString(R.string.workoutSpeed));
                    break;
                case PACE_CANDLE_DATA:
                    currentCandleDataSet = statsProvider.getPaceCandleData(aggregationSpan, workoutTypes);
                    setTitle(this.getString(R.string.workoutPace));
                    break;
                case DISTANCE_CANDLE_DATA:
                    currentCandleDataSet = statsProvider.getDistanceCandleData(aggregationSpan, workoutTypes);
                    setTitle(this.getString(R.string.workoutAvgDistance));
                    break;
                case DURATION_CANDLE_DATA:
                    currentCandleDataSet = statsProvider.getDurationCandleData(aggregationSpan, workoutTypes);
                    setTitle(this.getString(R.string.workoutAvgDurationLong));
                    break;
                case PAUSE_DURATION_CANDLE_DATA:
                    currentCandleDataSet = statsProvider.getPauseDurationCandleData(aggregationSpan, workoutTypes);
                    setTitle(this.getString(R.string.workoutAvgPauseDuration));
                    break;
                default:
                    break;
            }
            if (currentCandleDataSet != null) {
                combinedData.setData(new CandleData(currentCandleDataSet));
                // Create background line data
                LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(currentCandleDataSet);
                combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(this, lineDataSet)));
            }
        } catch (NoDataException e) {
        }

        // Draw bar charts
        try {
            switch (statsType)  {
                case DISTANCE_SUM_DATA:
                    currentBarDataSet = statsProvider.getDistanceSumData(aggregationSpan, workoutTypes);
                    setTitle(this.getString(R.string.workoutDistanceSum));
                    break;
                case DURATION_SUM_DATA:
                    currentBarDataSet = statsProvider.getDurationSumData(aggregationSpan, workoutTypes);
                    setTitle(this.getString(R.string.workoutDurationSum));
                    break;
                case PAUSE_DURATION_SUM_DATA:
                    currentBarDataSet = statsProvider.getPauseDurationSumData(aggregationSpan, workoutTypes);
                    setTitle(this.getString(R.string.workoutPauseDurationSum));
                    break;
            }
            if (currentBarDataSet != null) {
                BarData barData = new BarData(currentBarDataSet);
                ChartStyles.setTextAppearance(barData);
                barData.setBarWidth(aggregationSpan.spanInterval / stats_time_factor * ChartStyles.BAR_WIDTH_FACTOR);
                combinedData.setData(barData);
            }
        } catch (NoDataException e) {
        }

        chart.getXAxis().setValueFormatter(new FractionedDateFormatter(this,aggregationSpan));
        chart.getXAxis().setGranularity((float)aggregationSpan.spanInterval / stats_time_factor);
        ChartStyles.setXAxisLabel(chart, getString(aggregationSpan.axisLabel));

        chart.getAxisLeft().setValueFormatter(combinedData.getMaxEntryCountSet().getValueFormatter());

        if(chart.getLegend().getEntries().length>0) {
            String yLabel = chart.getLegend().getEntries()[0].label;
            chart.setMarker(new DisplayValueMarker(this, chart.getAxisLeft().getValueFormatter(), yLabel));
        }
        else
        {
            chart.setMarker(new DisplayValueMarker(this, chart.getAxisLeft().getValueFormatter(), ""));
        }

        chart.setData(combinedData);
        chart.invalidate();
    }

    private void animateChart (CombinedChart chart) {
        chart.animateY(500, Easing.EaseInExpo);
    }
}
