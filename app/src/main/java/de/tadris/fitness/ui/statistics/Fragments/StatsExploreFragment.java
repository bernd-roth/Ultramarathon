package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.data.preferences.UserPreferences;
import de.tadris.fitness.ui.statistics.TimeSpanSelection;
import de.tadris.fitness.ui.statistics.WorkoutTypeSelection;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.formatter.DayTimeFormatter;
import de.tadris.fitness.util.exceptions.NoDataException;

public class StatsExploreFragment extends StatsFragment {
    View overviewSpeed;
    View overviewSpeed2;
    View overviewDistance;
    View overviewDistance2;
    View overviewDuration;
    View overviewDuration2;
    View overviewExplore;
    View overviewExplore2;

    Spinner exploreTitle;

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

        overviewExplore = view.findViewById(R.id.overviewExplore);
        ((TextView)overviewExplore.findViewById(R.id.v1title)).setText(R.string.avg);
        ((TextView)overviewExplore.findViewById(R.id.v2title)).setText(R.string.sum);
        overviewExplore2 = view.findViewById(R.id.overviewExplore2);
        ((TextView)overviewExplore2.findViewById(R.id.v1title)).setText(R.string.min);
        ((TextView)overviewExplore2.findViewById(R.id.v2title)).setText(R.string.max);

        ((TextView)selection.findViewById(R.id.view_workout_type_selection_text)).setTextColor(getContext().getColor(R.color.textDarkerWhite));
        timeSpanSelection = view.findViewById(R.id.time_span_selection);
        timeSpanSelection.setForegroundColor(getContext().getColor(R.color.textDarkerWhite));
        timeSpanSelection.addOnTimeSpanSelectionListener(new TimeSpanSelection.OnTimeSpanSelectionListener() {
            @Override
            public void onTimeSpanChanged(AggregationSpan aggregationSpan, long instance) {
                updateOverview();
            }
        });

        exploreTitle = view.findViewById(R.id.stats_explore_title2);
        // Register WorkoutType selection listeners
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.stats_title, WorkoutProperty.getStringRepresentations(getContext()));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        exploreTitle.setAdapter(spinnerAdapter);
        exploreTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                WorkoutProperty property = WorkoutProperty.getById(exploreTitle.getSelectedItemPosition());
                long start = timeSpanSelection.getSelectedInstance();
                StatsDataTypes.TimeSpan span = new StatsDataTypes.TimeSpan(start, timeSpanSelection.getSelectedAggregationSpan().getAggregationEnd(start));
                updateOverviewExplore(property, span, selection.getSelectedWorkoutTypes());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        exploreTitle.setSelection(WorkoutProperty.PAUSE_DURATION.getId());

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

        try {
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
            updateOverviewExplore(WorkoutProperty.getById(exploreTitle.getSelectedItemPosition()), span, types);
        } catch (NoDataException e) {
            updateOverview(overviewSpeed, "", "");
            updateOverview(overviewSpeed2, "", "");
            updateOverview(overviewDistance, "", "");
            updateOverview(overviewDistance2, "", "");
            updateOverview(overviewDuration, "", "");
            updateOverview(overviewDuration2, "", "");
        }
    }

    private void updateOverview(View overview, String value1, String value2)
    {
        TextView v1 = overview.findViewById(R.id.v1value);
        TextView v2 = overview.findViewById(R.id.v2value);

        v1.setText(value1);
        v2.setText(value2);
    }

    private void updateOverviewExplore(WorkoutProperty property, StatsDataTypes.TimeSpan span, List<WorkoutType> types)
    {
        try {
            float sum = (float) statsProvider.getValue(span, types, property, StatsProvider.Reduction.SUM);
            float avg = (float) statsProvider.getValue(span, types, property, StatsProvider.Reduction.AVERAGE);
            if(property == WorkoutProperty.START || property == WorkoutProperty.END)
                updateOverview(overviewExplore, (new DayTimeFormatter(getContext())).getFormattedValue(avg), "");
            else
                updateOverview(overviewExplore, property.getFormattedValue(getContext(), avg),
                    property.getFormattedValue(getContext(), sum));

            if(property.summable()) {
                overviewExplore.findViewById(R.id.v2value).setVisibility(View.VISIBLE);
                overviewExplore.findViewById(R.id.v2title).setVisibility(View.VISIBLE);
            }
            else
            {
                overviewExplore.findViewById(R.id.v2value).setVisibility(View.INVISIBLE);
                overviewExplore.findViewById(R.id.v2title).setVisibility(View.INVISIBLE);
            }

            float minimum = (float) statsProvider.getValue(span, types, property, StatsProvider.Reduction.MINIMUM);
            float maximum = (float) statsProvider.getValue(span, types, property, StatsProvider.Reduction.MAXIMUM);
            if(property == WorkoutProperty.START || property == WorkoutProperty.END)
                updateOverview(overviewExplore2, (new DayTimeFormatter(getContext())).getFormattedValue(minimum),
                        (new DayTimeFormatter(getContext())).getFormattedValue(maximum));
            else
                updateOverview(overviewExplore2, property.getFormattedValue(getContext(), minimum),
                        property.getFormattedValue(getContext(), maximum));
        }
        catch (NoDataException e) {
            updateOverview(overviewExplore, "", "");
            updateOverview(overviewExplore2, "", "");
        }
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_explore_title);
    }
}