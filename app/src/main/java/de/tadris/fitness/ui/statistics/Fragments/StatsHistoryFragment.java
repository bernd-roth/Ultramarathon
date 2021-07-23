package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.tadris.fitness.R;
import de.tadris.fitness.ui.statistics.StatsProvider;

public class StatsHistoryFragment extends StatsFragment {

    StatsProvider statsProvider = new StatsProvider(context);
    ArrayList<CombinedChart> combinedChartList = new ArrayList<>();

    public StatsHistoryFragment(Context ctx) {
        super(R.layout.fragment_stats_history, ctx);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_history_title);
    }
}