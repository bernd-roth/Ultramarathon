package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.renderer.CombinedChartRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataProvider;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.ui.statistics.DetailStatsActivity;
import de.tadris.fitness.ui.statistics.TimeSpanSelection;
import de.tadris.fitness.ui.statistics.WorkoutTypeSelection;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.exceptions.NoDataException;
import de.tadris.fitness.util.statistics.OnChartGestureMultiListener;

public class StatsExploreFragment extends StatsFragment {
    View overviewSpeed;
    View overviewSpeed2;
    View overviewDistance;
    View overviewDistance2;
    View overviewDuration;
    View overviewDuration2;

    TimeSpanSelection timeSpanSelection;
    WorkoutTypeSelection selection;

    StatsProvider statsProvider;

    AggregationSpan aggregationSpan = AggregationSpan.YEAR;

    UserPreferences preferences;

    public StatsExploreFragment(Context ctx) {
        super(R.layout.fragment_stats_explore, ctx);
        statsProvider = new StatsProvider(ctx);
        preferences = new UserPreferences(ctx);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        selection = view.findViewById(R.id.stats_explore_type_selector);
        overviewSpeed = view.findViewById(R.id.overviewSpeed);
        ((TextView)overviewSpeed.findViewById(R.id.v1title)).setText(R.string.workoutAvgSpeedLong);
        ((TextView)overviewSpeed.findViewById(R.id.v2title)).setText(R.string.workoutPace);
        overviewSpeed2 = view.findViewById(R.id.overviewSpeed2);
        ((TextView)overviewSpeed2.findViewById(R.id.v1title)).setText(R.string.min);
        ((TextView)overviewSpeed2.findViewById(R.id.v2title)).setText(R.string.max);

        overviewDistance = view.findViewById(R.id.overviewDistance);
        ((TextView)overviewDistance.findViewById(R.id.v1title)).setText(R.string.workoutAvgDistance);
        ((TextView)overviewDistance.findViewById(R.id.v2title)).setText(R.string.workoutDistanceSum);
        overviewDistance2 = view.findViewById(R.id.overviewDistance2);
        ((TextView)overviewDistance2.findViewById(R.id.v1title)).setText(R.string.min);
        ((TextView)overviewDistance2.findViewById(R.id.v2title)).setText(R.string.max);

        overviewDuration = view.findViewById(R.id.overviewDuration);
        ((TextView)overviewDuration.findViewById(R.id.v1title)).setText(R.string.avg);
        ((TextView)overviewDuration.findViewById(R.id.v2title)).setText(R.string.sum);
        overviewDuration2 = view.findViewById(R.id.overviewDuration2);
        ((TextView)overviewDuration2.findViewById(R.id.v1title)).setText(R.string.min);
        ((TextView)overviewDuration2.findViewById(R.id.v2title)).setText(R.string.max);

        ((TextView)selection.findViewById(R.id.view_workout_type_selection_text)).setTextColor(getContext().getColor(R.color.textDarkerWhite));
        timeSpanSelection = view.findViewById(R.id.time_span_selection);
        timeSpanSelection.setForegroundColor(getContext().getColor(R.color.textDarkerWhite));
        timeSpanSelection.addOnTimeSpanSelectionListener(new TimeSpanSelection.OnTimeSpanSelectionListener() {
            @Override
            public void onTimeSpanChanged(AggregationSpan aggregationSpan, long instance) {
                updateOverview();
            }
        });

        selection.addOnWorkoutTypeSelectListener(workoutType -> updateOverview());
        selection.addOnWorkoutTypeSelectListener(workoutType -> preferences.setStatisticsSelectedTypes(selection.getSelectedWorkoutTypes()));
        List<WorkoutType> selected = preferences.getStatisticsSelectedTypes();
        if (selected.size()==0 || selected.get(0) == null) {
            selected.clear();
            selected.addAll(WorkoutTypeManager.getInstance().getAllTypes(context));
        }
        selection.setSelectedWorkoutTypes(selected);
    }

    private void updateOverview()
    {
        long start = timeSpanSelection.getSelectedInstance();
        StatsDataTypes.TimeSpan span = new StatsDataTypes.TimeSpan(start, timeSpanSelection.getSelectedAggregationSpan().getAggregationEnd(start));
        List<WorkoutType> types = selection.getSelectedWorkoutTypes();

        float avgSpeed = (float) statsProvider.getValue(span, types, WorkoutProperty.AVG_SPEED, StatsProvider.Reduction.AVERAGE);
        float avgPace = (float) statsProvider.getValue(span, types, WorkoutProperty.AVG_PACE, StatsProvider.Reduction.AVERAGE);
        updateOverview(overviewSpeed, WorkoutProperty.AVG_SPEED.getFormattedValue(getContext(), avgSpeed),
                WorkoutProperty.AVG_PACE.getFormattedValue(getContext(),avgPace));

        float lowSpeed = (float) statsProvider.getValue(span, types, WorkoutProperty.AVG_SPEED, StatsProvider.Reduction.MINIMUM);
        float highSpeed = (float) statsProvider.getValue(span, types, WorkoutProperty.AVG_SPEED, StatsProvider.Reduction.MAXIMUM);
        updateOverview(overviewSpeed2, WorkoutProperty.AVG_SPEED.getFormattedValue(getContext(), lowSpeed),
                WorkoutProperty.AVG_SPEED.getFormattedValue(getContext(), highSpeed));

        float sumDistance = (float) statsProvider.getValue(span, types, WorkoutProperty.LENGTH, StatsProvider.Reduction.SUM);
        float avgDistance = (float) statsProvider.getValue(span, types, WorkoutProperty.LENGTH, StatsProvider.Reduction.AVERAGE);
        updateOverview(overviewDistance, WorkoutProperty.LENGTH.getFormattedValue(getContext(), avgDistance),
                WorkoutProperty.LENGTH.getFormattedValue(getContext(), sumDistance));

        float lowDist = (float) statsProvider.getValue(span, types, WorkoutProperty.LENGTH, StatsProvider.Reduction.MINIMUM);
        float highDist = (float) statsProvider.getValue(span, types, WorkoutProperty.LENGTH, StatsProvider.Reduction.MAXIMUM);
        updateOverview(overviewDistance2, WorkoutProperty.LENGTH.getFormattedValue(getContext(), lowDist),
                WorkoutProperty.LENGTH.getFormattedValue(getContext(), highDist));

        float sumDuration = (float) statsProvider.getValue(span, types, WorkoutProperty.DURATION, StatsProvider.Reduction.SUM);
        float avgDuration = (float) statsProvider.getValue(span, types, WorkoutProperty.DURATION, StatsProvider.Reduction.AVERAGE);
        updateOverview(overviewDuration, WorkoutProperty.DURATION.getFormattedValue(getContext(), avgDuration),
                WorkoutProperty.DURATION.getFormattedValue(getContext(), sumDuration));

        float lowDur = (float) statsProvider.getValue(span, types, WorkoutProperty.DURATION, StatsProvider.Reduction.MINIMUM);
        float highDur = (float) statsProvider.getValue(span, types, WorkoutProperty.DURATION, StatsProvider.Reduction.MAXIMUM);
        updateOverview(overviewDuration2, WorkoutProperty.DURATION.getFormattedValue(getContext(), lowDur),
                WorkoutProperty.DURATION.getFormattedValue(getContext(), highDur));
    }

    private void updateOverview(View overview, String value1, String value2)
    {
        TextView v1 = overview.findViewById(R.id.v1value);
        TextView v2 = overview.findViewById(R.id.v2value);

        v1.setText(value1);
        v2.setText(value2);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_explore_title);
    }
}