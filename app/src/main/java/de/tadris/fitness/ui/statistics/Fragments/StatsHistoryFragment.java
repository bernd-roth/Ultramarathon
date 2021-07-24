package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
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


        CombinedData combinedPaceData = new CombinedData();
        combinedPaceData.setData(statsProvider.getPaceData(AggregationSpan.MONTH,
                WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_RUNNING)));

        CombinedChart distanceChart = view.findViewById(R.id.stats_dist_chart);
        distanceChart.setData(combinedPaceData);

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
    }

    private void updateSpeedChart() {
        CombinedData combinedData = new CombinedData();

        if (speedSwitch.isChecked()) {
            combinedData.setData(statsProvider.getPaceData(AggregationSpan.MONTH,
                    WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_RUNNING)));
        }  else {
            combinedData.setData(statsProvider.getSpeedData(AggregationSpan.MONTH,
                    WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_RUNNING)));
        }

        speedChart.setData(combinedData);
        speedChart.invalidate();
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_history_title);
    }
}