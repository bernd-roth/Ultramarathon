package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.StatsDataProvider;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.ui.statistics.TimeSpanSelection;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.exceptions.NoDataException;

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

        timeSpanSelection = view.findViewById(R.id.time_span_selection);
        timeSpanSelection.setForegroundColor(getContext().getColor(R.color.textDarkerWhite));

        numberOfActivitiesChart = view.findViewById(R.id.stats_number_of_workout_chart);
        distanceChart = view.findViewById(R.id.stats_distances_chart);

        StatsDataProvider statsDataProvider = new StatsDataProvider(getContext());
        long firstWorkoutTime;
        long lastWorkoutTime;
        try {
            firstWorkoutTime = statsDataProvider.getFirstData(WorkoutProperty.LENGTH, WorkoutTypeManager.getInstance().getAllTypes(context)).time;
            lastWorkoutTime = statsDataProvider.getLastData(WorkoutProperty.LENGTH, WorkoutTypeManager.getInstance().getAllTypes(context)).time;

        }
        catch (NoDataException e)
        {
            return;
        }

        timeSpanSelection.setLimits(firstWorkoutTime, lastWorkoutTime);
        timeSpanSelection.addOnTimeSpanSelectionListener((aggregationSpan, instance) -> updateCharts());

        ChartStyles.defaultBarChart(numberOfActivitiesChart);
        animateChart(numberOfActivitiesChart);

        ChartStyles.setXAxisLabel(distanceChart, Instance.getInstance(context).distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit());
        ChartStyles.defaultBarChart(distanceChart);
        animateChart(distanceChart);

        durationChart = view.findViewById(R.id.stats_duration_chart);
        ChartStyles.setXAxisLabel(durationChart, getContext().getString(R.string.timeHourShort));
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
        StatsDataTypes.TimeSpan span = new StatsDataTypes.TimeSpan(start, timeSpanSelection.getSelectedAggregationSpan().getAggregationEnd(start));

        try {
            BarData distanceData = new BarData(statsProvider.totalDistances(span));
            ChartStyles.horizontalBarChartIconLabel(distanceChart, distanceData, context);
        } catch (NoDataException e) {
            ChartStyles.barChartNoData(distanceChart, getContext());
        }
        distanceChart.invalidate();

        try {
            BarData numberOfActivitiesData = new BarData(statsProvider.numberOfActivities(span));
            ChartStyles.horizontalBarChartIconLabel(numberOfActivitiesChart,numberOfActivitiesData, context);
        } catch (NoDataException e) {
            ChartStyles.barChartNoData(numberOfActivitiesChart, getContext());
        }
        numberOfActivitiesChart.invalidate();

        try {
            BarData durationData = new BarData(statsProvider.totalDurations(span));
            ChartStyles.horizontalBarChartIconLabel(durationChart, durationData, context);
        } catch (NoDataException e) {
            ChartStyles.barChartNoData(durationChart, getContext());
        }
        durationChart.invalidate();
    }
}
