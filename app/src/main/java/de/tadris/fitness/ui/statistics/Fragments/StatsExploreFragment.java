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
    TextView lowestSpeed;
    TextView highestSpeed;
    TextView lowestDistance;
    TextView highestDistance;
    TextView lowestDuration;
    TextView highestDuration;

    View overviewSpeed;
    View overviewDistance;
    View overviewDuration;

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
        ((TextView)overviewSpeed.findViewById(R.id.v1title)).setText(R.string.workoutSpeed);
        ((TextView)overviewSpeed.findViewById(R.id.v2title)).setText(R.string.workoutPace);
        lowestSpeed = view.findViewById(R.id.textLowestSpeed);
        highestSpeed = view.findViewById(R.id.textHighestSpeed);
        lowestDuration = view.findViewById(R.id.textLowestDuration);
        highestDuration = view.findViewById(R.id.textHighestDuration);
        lowestDistance = view.findViewById(R.id.textLowestDistance);
        highestDistance = view.findViewById(R.id.textHighestDistance);

        overviewDistance = view.findViewById(R.id.overviewDistance);
        ((TextView)overviewDistance.findViewById(R.id.v1title)).setText(R.string.workoutAvgDistance);
        ((TextView)overviewDistance.findViewById(R.id.v2title)).setText(R.string.workoutDistanceSum);

        overviewDuration = view.findViewById(R.id.overviewDuration);
        ((TextView)overviewDuration.findViewById(R.id.v1title)).setText(R.string.workoutAvgDurationLong);
        ((TextView)overviewDuration.findViewById(R.id.v2title)).setText(R.string.workoutDurationSum);

        ((ImageView)view.findViewById(R.id.imageViewSpeed)).setColorFilter(getContext().getColor(R.color.colorPrimary));
        ((ImageView)view.findViewById(R.id.imageViewDistance)).setColorFilter(getContext().getColor(R.color.colorPrimary));
        ((ImageView)view.findViewById(R.id.imageViewDuration)).setColorFilter(getContext().getColor(R.color.colorPrimary));

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

        float sumDistance = (float) statsProvider.getValue(span, types, WorkoutProperty.LENGTH, StatsProvider.Reduction.SUM);
        float avgDistance = (float) statsProvider.getValue(span, types, WorkoutProperty.LENGTH, StatsProvider.Reduction.AVERAGE);
        updateOverview(overviewDistance, WorkoutProperty.LENGTH.getFormattedValue(getContext(), avgDistance),
                WorkoutProperty.LENGTH.getFormattedValue(getContext(), sumDistance));

        float sumDuration = (float) statsProvider.getValue(span, types, WorkoutProperty.DURATION, StatsProvider.Reduction.SUM);
        float avgDuration = (float) statsProvider.getValue(span, types, WorkoutProperty.DURATION, StatsProvider.Reduction.AVERAGE);
        updateOverview(overviewDuration, WorkoutProperty.DURATION.getFormattedValue(getContext(), avgDuration),
                WorkoutProperty.DURATION.getFormattedValue(getContext(), sumDuration));

        float lowSpeed = (float) statsProvider.getValue(span, types, WorkoutProperty.AVG_SPEED, StatsProvider.Reduction.MINIMUM);
        float highSpeed = (float) statsProvider.getValue(span, types, WorkoutProperty.AVG_SPEED, StatsProvider.Reduction.MAXIMUM);
        lowestSpeed.setText(WorkoutProperty.AVG_SPEED.getFormattedValue(getContext(), lowSpeed));
        highestSpeed.setText(WorkoutProperty.AVG_SPEED.getFormattedValue(getContext(), highSpeed));

        float lowDist = (float) statsProvider.getValue(span, types, WorkoutProperty.LENGTH, StatsProvider.Reduction.MINIMUM);
        float highDist = (float) statsProvider.getValue(span, types, WorkoutProperty.LENGTH, StatsProvider.Reduction.MAXIMUM);
        lowestDistance.setText(WorkoutProperty.LENGTH.getFormattedValue(getContext(), lowDist));
        highestDistance.setText(WorkoutProperty.LENGTH.getFormattedValue(getContext(), highDist));

        float lowDur = (float) statsProvider.getValue(span, types, WorkoutProperty.DURATION, StatsProvider.Reduction.MINIMUM);
        float highDur = (float) statsProvider.getValue(span, types, WorkoutProperty.DURATION, StatsProvider.Reduction.MAXIMUM);
        lowestDuration.setText(WorkoutProperty.DURATION.getFormattedValue(getContext(), lowDur));
        highestDuration.setText(WorkoutProperty.DURATION.getFormattedValue(getContext(), highDur));
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