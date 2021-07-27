package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.concurrent.TimeUnit;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.ui.FitoTrackActivity;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.exceptions.NoDataException;

public class DetailStatsActivity extends FitoTrackActivity {

    Context ctx = this;

    CombinedChart chart;

    WorkoutTypeManager workoutTypeManager = WorkoutTypeManager.getInstance();
    WorkoutType workoutType;
    AggregationSpan aggregationSpan = AggregationSpan.YEAR;

    StatsProvider statsProvider = new StatsProvider(ctx);

    TextView title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_detail);
        setTitle(getString(R.string.details));
        setupActionBar();
        title = findViewById(R.id.stats_detail_chart_title);

        chart = findViewById(R.id.stats_detail_chart);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String chartId = getIntent().getExtras().getString("chart");
        title.setText(chartId);
        String type = (String) getIntent().getSerializableExtra("type");

        if (type.equals("_all")) {
            workoutType = new WorkoutType();
            workoutType.id = type;
        }
        else
            workoutType = workoutTypeManager.getWorkoutTypeById(ctx, type);

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
                long timeSpan = (long) ((chart.getHighestVisibleX() - chart.getLowestVisibleX()) * R.fraction.stats_time_factor);
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
                    updateCharts(workoutType, chartId);
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });

        updateCharts(workoutType, chartId);
    }


    private void updateCharts(WorkoutType workoutType, String chartId) {

        CandleDataSet candleDataSet = null;

        if (chartId.equals(ctx.getString(R.string.workoutAvgSpeedShort))) {

            try {
                candleDataSet = statsProvider.getSpeedCandleData(aggregationSpan, workoutType);
            } catch (NoDataException e) {
                e.printStackTrace();
            }
        }
        else if (chartId.equals(ctx.getString(R.string.workoutDistance))) {

            try {
                candleDataSet = statsProvider.getDistanceCandleData(aggregationSpan, workoutType);
            } catch (NoDataException e) {
                e.printStackTrace();
            }
        }
        else if (chartId.equals(ctx.getString(R.string.workoutDuration))) {

            try {
                candleDataSet = statsProvider.getDurationCandleData(aggregationSpan, workoutType);
            } catch (NoDataException e) {
                e.printStackTrace();
            }
        }
        else if (chartId.equals(ctx.getString(R.string.workoutPauseDuration))) {

            try {
                candleDataSet = statsProvider.getPauseDurationCandleData(aggregationSpan, workoutType);
            } catch (NoDataException e) {
                e.printStackTrace();
            }
        }
        else if (chartId.equals(ctx.getString(R.string.workoutPace))) {

            try {
                candleDataSet = statsProvider.getPaceCandleData(aggregationSpan, workoutType);
            } catch (NoDataException e) {
                e.printStackTrace();
            }
        }


        // Set data for distance chart
        // Retrieve candle data

        CombinedData combinedData = new CombinedData();
        combinedData.setData(new CandleData(candleDataSet));

        // Create background line data
        LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
        combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(ctx, lineDataSet)));

        chart.setData(combinedData);
        chart.invalidate();
    }
}

