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
    Spinner title;
    Switch chartSwitch;
    CombinedChart chart;
    TextView highestViewDiagram;
    TextView lowestViewDiagram;

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
        title = view.findViewById(R.id.stats_explore_title);
        highestViewDiagram = view.findViewById(R.id.textHighestDiagram);
        lowestViewDiagram = view.findViewById(R.id.textLowestDiagram);
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
        chart = view.findViewById(R.id.stats_explore_chart);
        chartSwitch = view.findViewById(R.id.stats_explore_switch);
        timeSpanSelection = view.findViewById(R.id.time_span_selection);
        timeSpanSelection.setForegroundColor(getContext().getColor(R.color.textDarkerWhite));
        timeSpanSelection.addOnTimeSpanSelectionListener(new TimeSpanSelection.OnTimeSpanSelectionListener() {
            @Override
            public void onTimeSpanChanged(AggregationSpan aggregationSpan, long instance) {
                updateOverview();
            }
        });

        // Register WorkoutType selection listeners
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.stats_title, WorkoutProperty.getStringRepresentations(getContext()));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        title.setAdapter(spinnerAdapter);

        selection.addOnWorkoutTypeSelectListener(workoutType -> updateChart());
        selection.addOnWorkoutTypeSelectListener(workoutType -> updateOverview());
        selection.addOnWorkoutTypeSelectListener(workoutType -> preferences.setStatisticsSelectedTypes(selection.getSelectedWorkoutTypes()));
        List<WorkoutType> selected = preferences.getStatisticsSelectedTypes();
        if (selected.size()==0 || selected.get(0) == null) {
            selected.clear();
            selected.addAll(WorkoutTypeManager.getInstance().getAllTypes(context));
        }
        selection.setSelectedWorkoutTypes(selected);

        title.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                WorkoutProperty property = WorkoutProperty.getById(title.getSelectedItemPosition());
                if(!property.summable())
                {
                    chartSwitch.setChecked(false);
                    chartSwitch.setEnabled(false);
                    chartSwitch.setVisibility(View.GONE);
                }
                else
                {
                    chartSwitch.setEnabled(true);
                    chartSwitch.setVisibility(View.VISIBLE);
                }
                updateChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // Setup switch functionality
        chartSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateChart();
            }
        });
        ChartStyles.animateChart(chart);
        ChartStyles.fixViewPortOffsets(chart, 120);
        ChartStyles.defaultLineChart(chart);
        statsProvider.setAxisLimits(chart.getXAxis(), WorkoutProperty.TOP_SPEED);
        OnChartGestureMultiListener multiListener = new OnChartGestureMultiListener(new ArrayList<>());
        multiListener.listeners.add(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                Intent i = new Intent(context, DetailStatsActivity.class);
                i.putExtra("property", WorkoutProperty.getById(title.getSelectedItemPosition()));
                i.putExtra("summed", chartSwitch.isChecked());
                i.putExtra("types", (Serializable) selection.getSelectedWorkoutTypes());
                i.putExtra("formatter", chart.getAxisLeft().getValueFormatter().getClass());
                i.putExtra("xScale", chart.getScaleX());
                i.putExtra("xTrans", chart.getLowestVisibleX());
                i.putExtra("aggregationSpan", aggregationSpan);
                String label = "";
                if(chart.getLegend().getEntries().length>0)
                    label =chart.getLegend().getEntries()[0].label;
                i.putExtra("ylabel", label);
                context.startActivity(i);
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                AggregationSpan newAggSpan = ChartStyles.statsAggregationSpan(chart);
                if (aggregationSpan != newAggSpan) {
                    aggregationSpan = newAggSpan;
                    updateChart();
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });
        chart.setOnChartGestureListener(multiListener);

        displaySpan(preferences.getStatisticsAggregationSpan()); // set viewport according to other statistic views
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

    private void displaySpan(AggregationSpan span)
    {
        // set span for aggregation -> one smaller
        if(span == AggregationSpan.ALL){
            aggregationSpan = AggregationSpan.YEAR;
        } else if(span == AggregationSpan.YEAR){
            aggregationSpan = AggregationSpan.MONTH;
        } else if(span == AggregationSpan.MONTH){
            aggregationSpan = AggregationSpan.WEEK;
        } else if(span == AggregationSpan.WEEK){
            aggregationSpan = AggregationSpan.SINGLE;
        }
        updateChart();

        // set view port
        final StatsDataProvider dataProvider = new StatsDataProvider(context);
        final ArrayList<StatsDataTypes.DataPoint> data = dataProvider.getData(WorkoutProperty.LENGTH, selection.getSelectedWorkoutTypes());
        if (data.size() > 0) {
            final StatsDataTypes.DataPoint firstEntry = data.get(0);
            final StatsDataTypes.DataPoint lastEntry = data.get(data.size() - 1);
            final long leftTime = lastEntry.time - span.spanInterval;
            final float zoom = (float) (lastEntry.time- firstEntry.time) / span.spanInterval;
            chart.zoom(zoom, 1, 0, 0);
            chart.moveViewToX(leftTime);
        }
    }

    private void updateChart() {
        List<WorkoutType> workoutTypes = selection.getSelectedWorkoutTypes();
        WorkoutProperty property = WorkoutProperty.getById(title.getSelectedItemPosition());
        CombinedData combinedData = new CombinedData();
        String lowest, highest;

        try {
            if (chartSwitch.isChecked()) {
                BarDataSet barDataSet = statsProvider.getSumData(aggregationSpan, workoutTypes, property);
                highest = barDataSet.getValueFormatter().getFormattedValue(barDataSet.getYMax());
                lowest = barDataSet.getValueFormatter().getFormattedValue(barDataSet.getYMin());
                BarData barData = new BarData(barDataSet);
                combinedData.setData(barData);
            } else {
                CandleDataSet candleDataSet = statsProvider.getCandleData(aggregationSpan, workoutTypes, property);
                highest = candleDataSet.getValueFormatter().getFormattedValue(candleDataSet.getYMax());
                lowest = candleDataSet.getValueFormatter().getFormattedValue(candleDataSet.getYMin());
                combinedData.setData(new CandleData(candleDataSet));
                // Create background line
                LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
                combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(context, lineDataSet)));
            }

            // It is very dumb but CombinedChart.setData() calls the initBuffer method of all renderer before resetting the renderer (because the super call is executed before).
            // In case a bar chart was displayed before but not longer, the activity would crash.
            // Therefore the following two lines resets all renderers manually.
            chart.clear();
            ((CombinedChartRenderer) chart.getRenderer()).createRenderers();
            if(!(property == WorkoutProperty.START) && !(property == WorkoutProperty.END)) {
                String unit = property.getUnit(getContext(), combinedData.getYMax() - combinedData.getYMin());
                ChartStyles.setYAxisLabel(chart, unit);
                lowest += " " + unit;
                highest += " " + unit;
            }
            ChartStyles.updateCombinedChartToSpan(chart, combinedData, aggregationSpan, getContext());
            lowestViewDiagram.setText(lowest);
            highestViewDiagram.setText(highest);

        } catch (NoDataException e) {
            chart.clear();
        }
        chart.invalidate();
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_explore_title);
    }
}