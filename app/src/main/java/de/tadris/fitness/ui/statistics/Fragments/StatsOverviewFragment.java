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

import de.tadris.fitness.R;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;

public class StatsOverviewFragment extends StatsFragment {
    StatsProvider statsProvider = new StatsProvider(context);

    public StatsOverviewFragment(Context ctx) {
        super(R.layout.fragment_stats_overview, ctx);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HorizontalBarChart numberOfActivitiesChart = view.findViewById(R.id.stats_number_of_workout_chart);
        numberOfActivitiesChart.setData(statsProvider.numberOfActivities(new StatsDataTypes.TimeSpan(0, Long.MAX_VALUE)));
        numberOfActivitiesChart.getLegend().setEnabled(false);
        animateChart(numberOfActivitiesChart);

        HorizontalBarChart distanceChart = view.findViewById(R.id.stats_distances_chart);
        distanceChart.setData(statsProvider.totalDistances(new StatsDataTypes.TimeSpan(0, Long.MAX_VALUE)));
        distanceChart.getLegend().setEnabled(false);
        animateChart(distanceChart);

        HorizontalBarChart durationChart = view.findViewById(R.id.stats_duration_chart);
        durationChart.setData(statsProvider.totalDurations(new StatsDataTypes.TimeSpan(0, Long.MAX_VALUE)));
        durationChart.getLegend().setEnabled(false);
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
