package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;

import java.util.GregorianCalendar;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataProvider;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.ui.statistics.TimeSpanSelection;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.exceptions.NoDataException;
import de.tadris.fitness.util.statistics.InstanceFormatter;

public class StatsOverviewFragment extends StatsFragment {
    StatsProvider statsProvider;

    TimeSpanSelection timeSpanSelection;
    HorizontalBarChart distanceChart;
    HorizontalBarChart numberOfActivitiesChart;
    HorizontalBarChart durationChart;

    public StatsOverviewFragment(Context ctx) {
        super(R.layout.fragment_stats_overview, ctx);
        statsProvider = new StatsProvider(ctx);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StatsDataProvider statsDataProvider = new StatsDataProvider(getContext());
        long firstWorkoutTime = statsDataProvider.getFirstData(WorkoutProperty.LENGTH, WorkoutTypeManager.getInstance().getAllTypes(context)).time;
        long lastWorkoutTime = statsDataProvider.getLastData(WorkoutProperty.LENGTH, WorkoutTypeManager.getInstance().getAllTypes(context)).time;

        timeSpanSelection = view.findViewById(R.id.time_span_selection);
        timeSpanSelection.setLimits(firstWorkoutTime, lastWorkoutTime);
        timeSpanSelection.addOnTimeSpanSelectionListener((aggregationSpan, instance) -> updateCharts());

        numberOfActivitiesChart = view.findViewById(R.id.stats_number_of_workout_chart);
        ChartStyles.defaultBarChart(numberOfActivitiesChart);
        animateChart(numberOfActivitiesChart);

        distanceChart = view.findViewById(R.id.stats_distances_chart);
        ChartStyles.defaultBarChart(distanceChart);
        animateChart(distanceChart);

        durationChart = view.findViewById(R.id.stats_duration_chart);
        ChartStyles.defaultBarChart(durationChart);
        animateChart(durationChart);

        updateCharts();
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_overview_title);
    }

    private void animateChart (HorizontalBarChart chart) {
        chart.animateY(500, Easing.EaseInExpo);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateCharts() {
        long start = timeSpanSelection.getSelectedInstance();
        StatsDataTypes.TimeSpan span = new StatsDataTypes.TimeSpan(start,
                start + timeSpanSelection.getSelectedAggregationSpan().spanInterval);

        try {
            BarData distanceData = new BarData(statsProvider.totalDistances(span));
            ChartStyles.horizontalBarChartIconLabel(distanceChart, distanceData, context);
        } catch (NoDataException e) {
            distanceChart.clear();
        }
        distanceChart.invalidate();

        try {
            BarData numberOfActivitiesData = new BarData(statsProvider.numberOfActivities(span));
            ChartStyles.horizontalBarChartIconLabel(numberOfActivitiesChart,numberOfActivitiesData, context);
        } catch (NoDataException e) {
            numberOfActivitiesChart.clear();
        }
        numberOfActivitiesChart.invalidate();

        try {
            BarData durationData = new BarData(statsProvider.totalDurations(span));
            ChartStyles.horizontalBarChartIconLabel(durationChart, durationData, context);
        } catch (NoDataException e) {
            durationChart.clear();
        }
        durationChart.invalidate();
    }
}
