package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.aggregation.WorkoutTypeFilter;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.ui.dialog.SelectWorkoutTypeDialog;
import de.tadris.fitness.ui.statistics.WorkoutTypeSelection;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.exceptions.NoDataException;

public class StatsHistoryFragment extends StatsFragment {

    TextView speedTitle;
    Switch speedSwitch;
    CombinedChart speedChart;

    TextView durationTitle;
    Switch durationSwitch;
    CombinedChart durationChart;

    StatsProvider statsProvider = new StatsProvider(context);
    ArrayList<CombinedChart> combinedChartList = new ArrayList<>();

    public StatsHistoryFragment(Context ctx) {
        super(R.layout.fragment_stats_history, ctx);
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

        for (CombinedChart combinedChart : combinedChartList) {
            combinedChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewPortHandler viewPortHandler = combinedChart.getViewPortHandler();
                    float scaleX = viewPortHandler.getScaleX();
                    float scaleY = viewPortHandler.getScaleY();

                    //change chart type depending on zoom level
                }
            });
        }

        updateCharts(selection.getSelectedWorkoutType());
    }

    private void updateCharts(WorkoutType workoutType) {
        CombinedChart distanceChart = getView().findViewById(R.id.stats_dist_chart);

        try {
            // Set data for distance chart
            // Retrieve candle data
            CandleDataSet distanceCandleSet = statsProvider.getDistanceCandleData(AggregationSpan.MONTH, workoutType);

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
                candleDataSet = statsProvider.getPaceCandleData(AggregationSpan.MONTH, workoutType);
            } else {
                candleDataSet = statsProvider.getSpeedCandleData(AggregationSpan.MONTH, workoutType);
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
                candleDataSet = statsProvider.getPauseDurationCandleData(AggregationSpan.MONTH, workoutType);
            } else {
                candleDataSet = statsProvider.getDurationCandleData(AggregationSpan.MONTH, workoutType);
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