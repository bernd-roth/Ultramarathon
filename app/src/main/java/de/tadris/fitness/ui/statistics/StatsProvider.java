package de.tadris.fitness.ui.statistics;

import android.content.Context;

import androidx.appcompat.content.res.AppCompatResources;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.Icon;

public class StatsProvider {
    Context ctx;

    public StatsProvider(Context ctx) {
        this.ctx = ctx;
    }

    public BarData numberOfActivities(AggregationSpan timeSpan) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        int barNumber = 0;

        //Todo: replace by StatsDataProvider method
        HashMap<WorkoutType, Integer> dict = new HashMap<>();


        //Retrieve data and add to the list
        for (Map.Entry<WorkoutType, Integer> entry : dict.entrySet())
        {
            entries.add(new BarEntry(
                    (float)barNumber,
                    (float)entry.getValue(),
                    AppCompatResources.getDrawable(ctx, Icon.getIcon(entry.getKey().icon))));

            barNumber++;
        }

        BarDataSet barDataSet = new BarDataSet(entries, ctx.getString(R.string.numberOfWorkouts));
        return new BarData(barDataSet);
    }
}
