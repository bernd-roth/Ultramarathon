package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.aggregation.WorkoutTypeFilter;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.ui.dialog.SelectWorkoutTypeDialog;
import de.tadris.fitness.ui.statistics.WorkoutTypeSelection;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.exceptions.NoDataException;
import de.tadris.fitness.util.statistics.ChartSynchronizer;
import de.tadris.fitness.util.statistics.OnChartGestureMultiListener;

public class StatsHistoryFragment extends StatsFragment {

    TextView speedTitle;
    Switch speedSwitch;
    CombinedChart speedChart;

    TextView durationTitle;
    Switch durationSwitch;
    CombinedChart durationChart;

    CombinedChart distanceChart;

    ChartSynchronizer synchronizer;

    StatsProvider statsProvider;
    ArrayList<CombinedChart> combinedChartList = new ArrayList<>();

    AggregationSpan aggregationSpan = AggregationSpan.YEAR;

    public StatsHistoryFragment(Context ctx) {
        super(R.layout.fragment_stats_history, ctx);
        synchronizer = new ChartSynchronizer();
        statsProvider = new StatsProvider(ctx);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Register WorkoutType selection listeners
        WorkoutTypeSelection selection = view.findViewById(R.id.stats_history_workout_type_selector);
        selection.addOnWorkoutTypeSelectListener(workoutType -> updateCharts(workoutType));

        // Setup switch functionality
        speedTitle = view.findViewById(R.id.stats_history_speed_title);
        speedChart = view.findViewById(R.id.stats_speed_chart);
        speedSwitch = view.findViewById(R.id.speed_switch);

        speedSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (speedSwitch.isChecked()) {
                    speedTitle.setText(R.string.workoutPace);
                } else {
                    speedTitle.setText(R.string.workoutSpeed);
                }
                updateSpeedChart(selection.getSelectedWorkoutType());
            }
        });


        durationTitle = view.findViewById(R.id.stats_history_duration_title);
        durationChart = view.findViewById(R.id.stats_duration_chart);
        durationSwitch = view.findViewById(R.id.duration_switch);

        durationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (durationSwitch.isChecked()) {
                    durationTitle.setText(R.string.workoutPauseDuration);
                } else {
                    durationTitle.setText(R.string.workoutDuration);
                }
                updateDurationChart(selection.getSelectedWorkoutType());
            }
        });

        distanceChart = view.findViewById(R.id.stats_dist_chart);

        combinedChartList.add(speedChart);
        combinedChartList.add(distanceChart);
        combinedChartList.add(durationChart);

        for (CombinedChart combinedChart : combinedChartList) {
            OnChartGestureMultiListener multiListener = new OnChartGestureMultiListener(new ArrayList<>());
            multiListener.listeners.add(synchronizer.addChart(combinedChart));
            multiListener.listeners.add(new OnChartGestureListener() {
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
                    long timeSpan = (long) ((combinedChart.getHighestVisibleX() - combinedChart.getLowestVisibleX()) * R.fraction.stats_time_factor);
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
                        updateCharts(selection.getSelectedWorkoutType());
                    }
                }

                @Override
                public void onChartTranslate(MotionEvent me, float dX, float dY) {

                }
            });
            combinedChart.setOnChartGestureListener(multiListener);
        }

        updateCharts(selection.getSelectedWorkoutType());
    }

    private void updateCharts(WorkoutType workoutType) {
        try {
            // Set data for distance chart
            // Retrieve candle data
            CandleDataSet distanceCandleSet = statsProvider.getDistanceCandleData(aggregationSpan, workoutType);

            CombinedData combinedDistanceData = new CombinedData();
            combinedDistanceData.setData(new CandleData(distanceCandleSet));

            // Create background line data
            LineDataSet distanceLineSet = StatsProvider.convertCandleToMeanLineData(distanceCandleSet);
            combinedDistanceData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(context, distanceLineSet)));

            distanceChart.setData(combinedDistanceData);
        } catch (NoDataException e) {
            distanceChart.clear();
        }
        distanceChart.invalidate();

        updateSpeedChart(workoutType);
        updateDurationChart(workoutType);
    }

    private void updateSpeedChart(WorkoutType workoutType) {
        CandleDataSet candleDataSet;

        try {
            if (speedSwitch.isChecked()) {
                // Retrieve candle data
                candleDataSet = statsProvider.getPaceCandleData(aggregationSpan, workoutType);
            } else {
                candleDataSet = statsProvider.getSpeedCandleData(aggregationSpan, workoutType);
            }

            // Add candle data
            CombinedData combinedData = new CombinedData();
            combinedData.setData(new CandleData(candleDataSet));

            // Create background line
            LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
            combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(context, lineDataSet)));

            speedChart.setData(combinedData);
        } catch (NoDataException e) {
            speedChart.clear();
        }
        speedChart.invalidate();
    }

    private void updateDurationChart(WorkoutType workoutType) {
        CandleDataSet candleDataSet;

        try {
            if (durationSwitch.isChecked()) {
                candleDataSet = statsProvider.getPauseDurationCandleData(aggregationSpan, workoutType);
            } else {
                candleDataSet = statsProvider.getDurationCandleData(aggregationSpan, workoutType);
            }

            // Add candle data
            CombinedData combinedData = new CombinedData();
            combinedData.setData(new CandleData(candleDataSet));

            // Create background line
            LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
            combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(context, lineDataSet)));

            durationChart.setData(combinedData);
        } catch (NoDataException e) {
            durationChart.clear();
        }
        durationChart.invalidate();
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_history_title);
    }
}