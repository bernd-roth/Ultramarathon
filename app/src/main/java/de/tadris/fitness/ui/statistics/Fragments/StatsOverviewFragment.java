package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;

import de.tadris.fitness.R;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.util.charts.ChartStyles;

public class StatsOverviewFragment extends StatsFragment {
    StatsProvider statsProvider = new StatsProvider(context);

    public StatsOverviewFragment(Context ctx) {
        super(R.layout.fragment_stats_overview, ctx);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StatsDataTypes.TimeSpan allTime = new StatsDataTypes.TimeSpan(0, Long.MAX_VALUE);

        HorizontalBarChart numberOfActivitiesChart = view.findViewById(R.id.stats_number_of_workout_chart);
        ChartStyles.defaultBarChart(numberOfActivitiesChart);
        BarData numberOfActivitiesData =  new BarData(statsProvider.numberOfActivities(allTime));
        numberOfActivitiesChart.setData(numberOfActivitiesData);
        ChartStyles.horizontalBarChartIconLabel(numberOfActivitiesChart,numberOfActivitiesData, context);
        animateChart(numberOfActivitiesChart);

        HorizontalBarChart distanceChart = view.findViewById(R.id.stats_distances_chart);
        ChartStyles.defaultBarChart(distanceChart);
        BarData distanceData =  new BarData(statsProvider.totalDistances(allTime));
        distanceChart.setData(distanceData);
        ChartStyles.horizontalBarChartIconLabel(distanceChart, distanceData, context);
        animateChart(distanceChart);

        HorizontalBarChart durationChart = view.findViewById(R.id.stats_duration_chart);
        ChartStyles.defaultBarChart(durationChart);
        BarData durationData = new BarData(statsProvider.totalDurations(allTime));
        durationChart.setData(durationData);
        ChartStyles.horizontalBarChartIconLabel(durationChart, durationData, context);
        animateChart(durationChart);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_overview_title);
    }

    private void animateChart (HorizontalBarChart chart) {
        chart.animateY(2000, Easing.EaseInExpo);
    }
}
