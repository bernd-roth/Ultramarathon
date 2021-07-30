package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.graphics.Matrix;
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
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.concurrent.TimeUnit;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.ui.FitoTrackActivity;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.charts.DisplayValueMarker;
import de.tadris.fitness.util.charts.formatter.FractionedDateFormatter;
import de.tadris.fitness.util.charts.formatter.TimeFormatter;
import de.tadris.fitness.util.exceptions.NoDataException;

public class DetailStatsActivity extends FitoTrackActivity {

    CombinedChart chart;
    float stats_time_factor;
    WorkoutTypeManager workoutTypeManager;
    WorkoutType workoutType;
    AggregationSpan aggregationSpan;
    StatsProvider statsProvider;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        int chartIndex = Integer.parseInt(getIntent().getExtras().getString("data"));
        String type = (String) getIntent().getSerializableExtra("type");
        String label = (String) getIntent().getSerializableExtra("ylabel");
        Object formatterClass = (Object) getIntent().getSerializableExtra("formatter");
        float[] viewPortValues = getIntent().getFloatArrayExtra("viewPort");
        aggregationSpan = (AggregationSpan) getIntent().getSerializableExtra("aggregationSpan");

        ChartStyles.defaultLineChart(chart);
        ChartStyles.setYAxisLabel(chart,label);
        chart.setMarker(new DisplayValueMarker(this, chart.getAxisLeft().getValueFormatter(), label));

        if(formatterClass == TimeFormatter.class)
            chart.getAxisLeft().setValueFormatter(new TimeFormatter(TimeUnit.MINUTES, true, true, false));
        else
            chart.getAxisLeft().setValueFormatter(new DefaultValueFormatter(2));

        if (type.equals("_all")) {
            workoutType = new WorkoutType();
            workoutType.id = type;
        }
        else
            workoutType = workoutTypeManager.getWorkoutTypeById(this, type);

        chart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

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
                    updateChart(workoutType, chartIndex);
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });

        chart.setMarker(new DisplayValueMarker(this, chart.getAxisLeft().getValueFormatter(), label));
        updateChart(workoutType, chartIndex);

        float[] currentViewPortValues = new float[9];
        chart.getViewPortHandler().getMatrixTouch().getValues(currentViewPortValues);

        currentViewPortValues[Matrix.MSCALE_X] = viewPortValues[Matrix.MSCALE_X];
        currentViewPortValues[Matrix.MTRANS_X] = viewPortValues[Matrix.MTRANS_X];

        animateChart(chart);
    }


    private void updateChart(WorkoutType workoutType, int chartIndex) {

        CombinedData combinedData = new CombinedData();

        // Draw candle charts
        CandleDataSet candleDataSet = null;
        try {
            switch (StatsProvider.StatsType.getByIndex(chartIndex)) {
                case SPEED_CANDLE_DATA:
                    candleDataSet = statsProvider.getSpeedCandleData(aggregationSpan, workoutType);
                    setTitle(this.getString(R.string.workoutSpeed));
                    break;
                case PACE_CANDLE_DATA:
                    candleDataSet = statsProvider.getPaceCandleData(aggregationSpan, workoutType);
                    setTitle(this.getString(R.string.workoutPace));
                    break;
                case DISTANCE_CANDLE_DATA:
                    candleDataSet = statsProvider.getDistanceCandleData(aggregationSpan, workoutType);
                    setTitle(this.getString(R.string.workoutAvgDistance));
                    break;
                case DURATION_CANDLE_DATA:
                    candleDataSet = statsProvider.getDurationCandleData(aggregationSpan, workoutType);
                    setTitle(this.getString(R.string.workoutAvgDurationLong));
                    break;
                case PAUSE_DURATION_CANDLE_DATA:
                    candleDataSet = statsProvider.getPauseDurationCandleData(aggregationSpan, workoutType);
                    setTitle(this.getString(R.string.workoutAvgPauseDuration));
                    break;
                default:
                    break;
            }
            if (candleDataSet != null) {
                combinedData.setData(new CandleData(candleDataSet));
                // Create background line data
                LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
                combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(this, lineDataSet)));
            }
        } catch (NoDataException e) {
        }

        // Draw bar charts
        BarDataSet barDataSet = null;
        try {
            switch (StatsProvider.StatsType.getByIndex(chartIndex))  {
                case DISTANCE_SUM_DATA:
                    barDataSet = statsProvider.getDistanceSumData(aggregationSpan, workoutType);
                    setTitle(this.getString(R.string.workoutDistanceSum));
                    break;
                case DURATION_SUM_DATA:
                    barDataSet = statsProvider.getDurationSumData(aggregationSpan, workoutType);
                    setTitle(this.getString(R.string.workoutDurationSum));
                    break;
                case PAUSE_DURATION_SUM_DATA:
                    barDataSet = statsProvider.getPauseDurationSumData(aggregationSpan, workoutType);
                    setTitle(this.getString(R.string.workoutPauseDurationSum));
                    break;
            }
            if (barDataSet != null) {
                combinedData.setData(new BarData(barDataSet));
            }
        } catch (NoDataException e) {
        }

        chart.getXAxis().setValueFormatter(new FractionedDateFormatter(this,aggregationSpan));
        chart.getXAxis().setGranularity((float)aggregationSpan.spanInterval / stats_time_factor);
        ChartStyles.setXAxisLabel(chart, getString(aggregationSpan.axisLabel));

        chart.setData(combinedData);
        chart.invalidate();
    }

    private void animateChart (CombinedChart chart) {
        chart.animateY(500, Easing.EaseInExpo);
    }
}
