package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

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

        HorizontalBarChart numberOfActivitiesChart = view.findViewById(R.id.stats_number_of_workout_chart);
        ChartStyles.defaultBarChart(numberOfActivitiesChart);

        BarData numberOfActivitiesData = statsProvider.numberOfActivities(new StatsDataTypes.TimeSpan(0, Long.MAX_VALUE));
        numberOfActivitiesChart.setData(numberOfActivitiesData);

        HorizontalBarChart distanceChart = view.findViewById(R.id.stats_distances_chart);
        ChartStyles.defaultBarChart(distanceChart);
        distanceChart.setData(statsProvider.totalDistances(new StatsDataTypes.TimeSpan(0, Long.MAX_VALUE)));

        HorizontalBarChart durationChart = view.findViewById(R.id.stats_duration_chart);
        ChartStyles.defaultBarChart(durationChart);
        durationChart.setData(statsProvider.totalDurations(new StatsDataTypes.TimeSpan(0, Long.MAX_VALUE)));
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_overview_title);
    }
}
