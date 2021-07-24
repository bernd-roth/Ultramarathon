package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.ui.statistics.StatsProvider;

public class StatsHistoryFragment extends StatsFragment {

    StatsProvider statsProvider = new StatsProvider(context);
    ArrayList<CombinedChart> combinedChartList = new ArrayList<>();
    View typeSelector;
    TextView workoutTypeText;

    public StatsHistoryFragment(Context ctx) {
        super(R.layout.fragment_stats_history, ctx);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        typeSelector = view.findViewById(R.id.fragmentHistoryTypeSelector);
        workoutTypeText = view.findViewById(R.id.fragmentHistoryTypeTitle);


        CandleStickChart candleStickChart = view.findViewById(R.id.stats_speed_chart);
        candleStickChart.setData(statsProvider.getPaceData(AggregationSpan.MONTH,
                WorkoutType.getWorkoutTypeById(context, WorkoutType.WORKOUT_TYPE_ID_RUNNING)));
        /*
        CombinedChart speedChart = view.findViewById(R.id.stats_speed_chart);
        speedChart.setData(statsProvider.(new StatsDataTypes.TimeSpan(0, Long.MAX_VALUE)));
*/

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