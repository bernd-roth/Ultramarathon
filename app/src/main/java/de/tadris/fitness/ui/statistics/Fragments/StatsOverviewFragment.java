package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.CombinedData;

import org.jetbrains.annotations.NotNull;

import de.tadris.fitness.R;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.ui.statistics.StatsProvider;

public class StatsOverviewFragment extends StatsFragment {

    StatsProvider statsProvider = new StatsProvider(context);

    public StatsOverviewFragment(Context ctx) {
        super(R.layout.fragment_stats_overview, ctx);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CombinedChart numberOfActivitiesChart = view.findViewById(R.id.stats_number_of_workout_chart);
        BarData barData = statsProvider.numberOfActivities(new StatsDataTypes.TimeSpan(0, Long.MAX_VALUE));
        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        numberOfActivitiesChart.setData(combinedData);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_overview_title);
    }
}
