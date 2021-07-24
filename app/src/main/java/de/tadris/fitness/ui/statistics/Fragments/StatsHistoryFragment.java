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
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.StatsProvider;

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
                updateSpeedChart();
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
                updateDurationChart();
            }
        });


        // Set data for distance chart
        // Retrieve candle data
        CandleDataSet distanceCandleSet = statsProvider.getDistanceCandleData(AggregationSpan.MONTH,
                WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_RUNNING));

        CombinedData combinedDistanceData = new CombinedData();
        combinedDistanceData.setData(new CandleData(distanceCandleSet));

        // Create background line data
        LineDataSet distanceLineSet = StatsProvider.convertCandleToMeanLineData(distanceCandleSet);
        combinedDistanceData.setData(new LineData(statsProvider.applyBackgroundLineStyle(distanceLineSet)));

        CombinedChart distanceChart = view.findViewById(R.id.stats_dist_chart);
        distanceChart.setData(combinedDistanceData);

        for (CombinedChart combinedChart: combinedChartList) {
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

        updateSpeedChart();
        updateDurationChart();
    }

    private void updateSpeedChart() {
        CandleDataSet candleDataSet;

        if (speedSwitch.isChecked()) {
            // Retrieve candle data
            candleDataSet = statsProvider.getPaceCandleData(AggregationSpan.MONTH,
                    WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_RUNNING));
        }  else {
            candleDataSet = statsProvider.getSpeedCandleData(AggregationSpan.MONTH,
                    WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_RUNNING));
        }

        // Add candle data
        CombinedData combinedData = new CombinedData();
        combinedData.setData(new CandleData(candleDataSet));

        // Create background line
        LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
        combinedData.setData(new LineData(statsProvider.applyBackgroundLineStyle(lineDataSet)));

        speedChart.setData(combinedData);
        speedChart.invalidate();
    }

    private void updateDurationChart() {
        CandleDataSet candleDataSet;

        if (durationSwitch.isChecked()) {
            candleDataSet = statsProvider.getPauseDurationCandleData(AggregationSpan.MONTH,
                    WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_RUNNING));
        }  else {
            candleDataSet = statsProvider.getDurationCandleData(AggregationSpan.MONTH,
                    WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_RUNNING));
        }

        // Add candle data
        CombinedData combinedData = new CombinedData();
        combinedData.setData(new CandleData(candleDataSet));

        // Create background line
        LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
        combinedData.setData(new LineData(statsProvider.applyBackgroundLineStyle(lineDataSet)));

        durationChart.setData(combinedData);
        durationChart.invalidate();
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_history_title);
    }
}