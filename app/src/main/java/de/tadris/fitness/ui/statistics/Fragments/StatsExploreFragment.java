package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.renderer.CombinedChartRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataProvider;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.ui.statistics.DetailStatsActivity;
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
        selection = view.findViewById(R.id.stats_explore_type_selector);
        ((TextView)selection.findViewById(R.id.view_workout_type_selection_text)).setTextColor(getContext().getColor(R.color.textDarkerWhite));
        chart = view.findViewById(R.id.stats_explore_chart);
        chartSwitch = view.findViewById(R.id.stats_explore_switch);

        // Register WorkoutType selection listeners
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.stats_title, WorkoutProperty.getStringRepresentations(getContext()));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        title.setAdapter(spinnerAdapter);

        selection.addOnWorkoutTypeSelectListener(workoutType -> updateChart());
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

        try {
            if (chartSwitch.isChecked()) {
                BarDataSet barDataSet = statsProvider.getSumData(aggregationSpan, workoutTypes, property);;
                BarData barData = new BarData(barDataSet);
                combinedData.setData(barData);
            } else {
                CandleDataSet candleDataSet = statsProvider.getCandleData(aggregationSpan, workoutTypes, property);
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
            ChartStyles.updateCombinedChartToSpan(chart, combinedData, aggregationSpan, getContext());
            if(!(property == WorkoutProperty.START) && !(property ==WorkoutProperty.END)) {
                ChartStyles.setYAxisLabel(chart, property.getUnit(getContext(), combinedData.getYMax() - combinedData.getYMin()));
            }

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