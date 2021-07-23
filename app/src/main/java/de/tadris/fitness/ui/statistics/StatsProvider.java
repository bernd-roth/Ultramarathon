package de.tadris.fitness.ui.statistics;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tadris.fitness.R;
import de.tadris.fitness.data.StatsDataProvider;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.util.Icon;
import de.tadris.fitness.util.WorkoutProperty;

public class StatsProvider {

    Context ctx;
    StatsDataProvider dataProvider;

    public StatsProvider(Context ctx) {
        this.ctx = ctx;
        dataProvider = new StatsDataProvider(ctx);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarData numberOfActivities(StatsDataTypes.TimeSpan timeSpan) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Integer> numberOfWorkouts = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WorkoutProperty.LENGTH, WorkoutType.getAllTypes(ctx));

        // Count number of workouts of specific WorkoutType in a specific time span
        for (StatsDataTypes.DataPoint dataPoint : workouts) {
            if (timeSpan.contains(dataPoint.time)) {

                numberOfWorkouts.put(dataPoint.workoutType,
                        numberOfWorkouts.getOrDefault(dataPoint.workoutType, 0) + 1);
            }
        }

        for (Map.Entry<WorkoutType, Integer> entry : numberOfWorkouts.entrySet()) {

            barEntries.add(new BarEntry(
                    (float)barNumber,
                    entry.getValue(),
                    AppCompatResources.getDrawable(ctx, Icon.getIcon(entry.getKey().icon))));

            barNumber++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, ctx.getString(R.string.numberOfWorkouts));
        return new BarData(barDataSet);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarData totalDistances(StatsDataTypes.TimeSpan timeSpan) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        int barNumber = 0;

        HashMap<WorkoutType, Float> distances = new HashMap<>();

        ArrayList<StatsDataTypes.DataPoint> workouts = dataProvider.getData(WorkoutProperty.LENGTH, WorkoutType.getAllTypes(ctx));

        for (StatsDataTypes.DataPoint dataPoint : workouts) {
            if (timeSpan.contains(dataPoint.time)) {
                distances.put(dataPoint.workoutType,
                        distances.getOrDefault(dataPoint.workoutType, (float)0) + (float)dataPoint.value);
            }
        }

        //Retrieve data and add to the list
        for (Map.Entry<WorkoutType, Float> entry : distances.entrySet()) {

            barEntries.add(new BarEntry(
                    (float)barNumber,
                    entry.getValue(),
                    AppCompatResources.getDrawable(ctx, Icon.getIcon(entry.getKey().icon))));

            barNumber++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, ctx.getString(R.string.distances));
        return new BarData(barDataSet);
    }
}
