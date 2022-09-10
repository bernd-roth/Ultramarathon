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
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.data.preferences.UserPreferences;
import de.tadris.fitness.ui.statistics.DetailStatsActivity;
import de.tadris.fitness.ui.statistics.WorkoutTypeSelection;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.exceptions.NoDataException;
import de.tadris.fitness.util.statistics.ChartSynchronizer;
import de.tadris.fitness.util.statistics.OnChartGestureMultiListener;

public class StatsHistoryFragment extends StatsFragment {
    TextView speedTitle;
    Switch speedSwitch;
    CombinedChart speedChart;

    TextView durationTitle;
    Switch durationSwitch;
    CombinedChart durationChart;

    Spinner exploreTitle;
    Switch exploreChartSwitch;
    CombinedChart exploreChart;


    TextView distanceTitle;
    Switch distanceSwitch;
    CombinedChart distanceChart;

    WorkoutTypeSelection selection;

    ChartSynchronizer synchronizer;

    StatsProvider statsProvider;
    ArrayList<CombinedChart> combinedChartList = new ArrayList<>();

    AggregationSpan aggregationSpan = AggregationSpan.YEAR;

    UserPreferences preferences;

    public StatsHistoryFragment(Context ctx) {
        super(R.layout.fragment_stats_history, ctx);
        synchronizer = new ChartSynchronizer();
        statsProvider = new StatsProvider(ctx);
        preferences = new UserPreferences(ctx);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Register WorkoutType selection listeners
        selection = view.findViewById(R.id.stats_history_workout_type_selector);
        ((TextView)selection.findViewById(R.id.view_workout_type_selection_text)).setTextColor(getContext().getColor(R.color.textDarkerWhite));
        selection.addOnWorkoutTypeSelectListener(workoutType -> updateCharts(selection.getSelectedWorkoutTypes()));
        selection.addOnWorkoutTypeSelectListener(workoutType -> preferences.setStatisticsSelectedTypes(selection.getSelectedWorkoutTypes()));

        // Setup switch functionality
        speedTitle = view.findViewById(R.id.stats_history_speed_title);
        speedChart = view.findViewById(R.id.stats_speed_chart);
        speedChart.setDoubleTapToZoomEnabled(false);
        speedSwitch = view.findViewById(R.id.speed_switch);
        speedSwitch.setOnClickListener(view14 -> {
            if (speedSwitch.isChecked()) {
                speedTitle.setText(R.string.workoutPace);
            } else {
                speedTitle.setText(R.string.workoutSpeed);
            }
            updateSpeedChart(selection.getSelectedWorkoutTypes());
        });

        distanceTitle = view.findViewById(R.id.stats_history_distance_title);
        distanceChart = view.findViewById(R.id.stats_history_distance_chart);
        distanceChart.setDoubleTapToZoomEnabled(false);
        distanceSwitch = view.findViewById(R.id.distance_switch);
        distanceSwitch.setOnClickListener(view13 -> {
            if (distanceSwitch.isChecked()) {
                distanceTitle.setText(R.string.workoutDistanceSum);
            } else {
                distanceTitle.setText(R.string.workoutAvgDistance);
            }
            updateDistanceChart(selection.getSelectedWorkoutTypes());
        });

        durationTitle = view.findViewById(R.id.stats_history_duration_title);
        durationChart = view.findViewById(R.id.stats_duration_chart);
        durationChart.setDoubleTapToZoomEnabled(false);
        durationSwitch = view.findViewById(R.id.duration_switch);
        durationSwitch.setOnClickListener(view12 -> {
            if (durationSwitch.isChecked()) {
                durationTitle.setText(R.string.workoutDurationSum);
            } else {
                durationTitle.setText(R.string.workoutAvgDurationLong);
            }
            updateDurationChart(selection.getSelectedWorkoutTypes());
        });

        exploreTitle = view.findViewById(R.id.stats_explore_title);
        // Register WorkoutType selection listeners
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.stats_title, WorkoutProperty.getStringRepresentations(getContext()));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        exploreTitle.setAdapter(spinnerAdapter);
        exploreTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                WorkoutProperty property = WorkoutProperty.getById(exploreTitle.getSelectedItemPosition());
                if(!property.summable())
                {
                    exploreChartSwitch.setChecked(false);
                    exploreChartSwitch.setEnabled(false);
                    exploreChartSwitch.setVisibility(View.GONE);
                }
                else
                {
                    exploreChartSwitch.setEnabled(true);
                    exploreChartSwitch.setVisibility(View.VISIBLE);
                }
                updateExploreChart(selection.getSelectedWorkoutTypes());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        exploreChart = view.findViewById(R.id.stats_explore_chart);
        exploreChart.setDoubleTapToZoomEnabled(false);
        exploreChartSwitch = view.findViewById(R.id.stats_explore_switch);
        exploreChartSwitch.setOnClickListener(view1 -> updateExploreChart(selection.getSelectedWorkoutTypes()));


        combinedChartList.add(speedChart);
        combinedChartList.add(distanceChart);
        combinedChartList.add(durationChart);
        combinedChartList.add(exploreChart);

        for (CombinedChart combinedChart : combinedChartList) {
            ChartStyles.animateChart(combinedChart);
            ChartStyles.fixViewPortOffsets(combinedChart, 120);
            ChartStyles.defaultLineChart(combinedChart);
            statsProvider.setAxisLimits(combinedChart.getXAxis(), WorkoutProperty.TOP_SPEED);
            OnChartGestureMultiListener multiListener = new OnChartGestureMultiListener(new ArrayList<>());
            multiListener.listeners.add(synchronizer.addChart(combinedChart));
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
                    float[] viewPortValues = new float[9];
                    combinedChart.getViewPortHandler().getMatrixTouch().getValues(viewPortValues);

                    Intent i = new Intent(context, DetailStatsActivity.class);
                    WorkoutProperty workoutProperty;
                    boolean summed = false;
                    switch (combinedChartList.indexOf(combinedChart))
                    {
                        case 0:
                            workoutProperty = speedSwitch.isChecked() ? WorkoutProperty.AVG_PACE : WorkoutProperty.AVG_SPEED;
                            break;
                        case 1:
                            workoutProperty = WorkoutProperty.LENGTH;
                            summed = distanceSwitch.isChecked();
                            break;
                        case 2:
                            workoutProperty = WorkoutProperty.DURATION;
                            summed = durationSwitch.isChecked();
                            break;
                        case 3:
                        default:
                            workoutProperty = WorkoutProperty.getById(exploreTitle.getSelectedItemPosition());
                            summed = exploreChartSwitch.isChecked();
                    }
                    i.putExtra("property", (Serializable) workoutProperty);
                    i.putExtra("summed", summed);
                    i.putExtra("types", (Serializable) selection.getSelectedWorkoutTypes());
                    i.putExtra("formatter", combinedChart.getAxisLeft().getValueFormatter().getClass());
                    //i.putExtra("viewPort", viewPortValues);
                    i.putExtra("xScale", combinedChart.getScaleX());
                    i.putExtra("xTrans", combinedChart.getLowestVisibleX());
                    i.putExtra("aggregationSpan", aggregationSpan);
                    String label = "";
                    if(combinedChart.getLegend().getEntries().length>0)
                        label =combinedChart.getLegend().getEntries()[0].label;
                    i.putExtra("ylabel", label);
                    context.startActivity(i);
                }

                @Override
                public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

                }

                @Override
                public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                    scaleChart(combinedChart);
                }

                @Override
                public void onChartTranslate(MotionEvent me, float dX, float dY) {

                }
            });
            combinedChart.setOnChartGestureListener(multiListener);
        }

        List<WorkoutType> selected = preferences.getStatisticsSelectedTypes();
        if (selected.size()==0 || selected.get(0) == null) {
            selected.clear();
            selected.addAll(WorkoutTypeManager.getInstance().getAllTypes(context));
        }
        selection.setSelectedWorkoutTypes(selected);

        displaySpan(preferences.getStatisticsAggregationSpan()); // set viewport according to other statistic views
        exploreTitle.setSelection(WorkoutProperty.PAUSE_DURATION.getId());
    }

    private void scaleChart(CombinedChart chart)
    {
        AggregationSpan newAggSpan = ChartStyles.statsAggregationSpan(chart);
        if (aggregationSpan != newAggSpan) {
            aggregationSpan = newAggSpan;
            updateCharts(selection.getSelectedWorkoutTypes());
        }
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
        updateCharts(selection.getSelectedWorkoutTypes());

        // set view port
        final StatsDataProvider dataProvider = new StatsDataProvider(context);
        final ArrayList<StatsDataTypes.DataPoint> data = dataProvider.getData(WorkoutProperty.LENGTH, selection.getSelectedWorkoutTypes());
        if (data.size() > 0) {
            final StatsDataTypes.DataPoint firstEntry = data.get(0);
            final StatsDataTypes.DataPoint lastEntry = data.get(data.size() - 1);
            final long leftTime = lastEntry.time - span.spanInterval;
            final float zoom = (float) (lastEntry.time- firstEntry.time) / span.spanInterval;
            for (CombinedChart chart:combinedChartList) { // need to iterate cause code zoom doesn't trigger GestureListeners
                chart.zoom(zoom, 1, 0, 0);
                chart.moveViewToX(leftTime);
            }
        }
    }

    private void updateCharts(List<WorkoutType> workoutTypes) {
        updateSpeedChart(workoutTypes);
        updateDurationChart(workoutTypes);
        updateExploreChart(workoutTypes);
        updateDistanceChart(workoutTypes);
    }

    private void updateSpeedChart(List<WorkoutType> workoutTypes) {
        CandleDataSet candleDataSet;

        WorkoutProperty property = speedSwitch.isChecked() ? WorkoutProperty.AVG_PACE : WorkoutProperty.AVG_SPEED;
        try {
            // Retrieve candle data
            candleDataSet = statsProvider.getCandleData(aggregationSpan, workoutTypes, property);
            ChartStyles.setYAxisLabel(speedChart, property.getUnit(context, candleDataSet.getYMax()));

            // Add candle data
            CombinedData combinedData = new CombinedData();
            combinedData.setData(new CandleData(candleDataSet));

            // Create background line
            LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
            combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(context, lineDataSet)));

            ChartStyles.updateStatsHistoryCombinedChartToSpan(speedChart, combinedData, aggregationSpan, getContext());
        } catch (NoDataException e) {
            speedChart.clear();
        }
        speedChart.invalidate();
    }

    private void updateDistanceChart(List<WorkoutType> workoutTypes) {
        CombinedData combinedData = new CombinedData();

        try {
            if (distanceSwitch.isChecked()) {
                BarDataSet barDataSet = statsProvider.getSumData(aggregationSpan, workoutTypes, WorkoutProperty.LENGTH);
                BarData barData = new BarData(barDataSet);
                combinedData.setData(barData);
            } else {
                CandleDataSet candleDataSet = statsProvider.getCandleData(aggregationSpan, workoutTypes, WorkoutProperty.LENGTH);
                combinedData.setData(new CandleData(candleDataSet));
                // Create background line
                LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
                combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(context, lineDataSet)));
            }

            // It is very dumb but CombinedChart.setData() calls the initBuffer method of all renderer before resetting the renderer (because the super call is executed before).
            // In case a bar chart was displayed before but not longer, the activity would crash.
            // Therefore the following two lines resets all renderers manually.
            distanceChart.clear();
            ((CombinedChartRenderer) distanceChart.getRenderer()).createRenderers();
            ChartStyles.updateStatsHistoryCombinedChartToSpan(distanceChart, combinedData, aggregationSpan, getContext());
            ChartStyles.setYAxisLabel(distanceChart, WorkoutProperty.LENGTH.getUnit(context, combinedData.getYMax()));
        } catch (NoDataException e) {
            distanceChart.clear();
        }
        distanceChart.invalidate();
    }

    private void updateDurationChart(List<WorkoutType> workoutTypes) {
        CombinedData combinedData = new CombinedData();

        try {
            if (durationSwitch.isChecked()) {
                BarDataSet barDataSet = statsProvider.getSumData(aggregationSpan, workoutTypes, WorkoutProperty.DURATION);
                BarData barData = new BarData(barDataSet);
                combinedData.setData(barData);
            } else {
                CandleDataSet candleDataSet = statsProvider.getCandleData(aggregationSpan, workoutTypes, WorkoutProperty.DURATION);
                combinedData.setData(new CandleData(candleDataSet));
                // Create background line
                LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
                combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(context, lineDataSet)));
            }

            // It is very dumb but CombinedChart.setData() calls the initBuffer method of all renderer before resetting the renderer (because the super call is executed before).
            // In case a bar chart was displayed before but not longer, the activity would crash.
            // Therefore the following two lines resets all renderers manually.
            durationChart.clear();
            ((CombinedChartRenderer) durationChart.getRenderer()).createRenderers();
            ChartStyles.updateStatsHistoryCombinedChartToSpan(durationChart, combinedData, aggregationSpan, getContext());
            ChartStyles.setYAxisLabel(durationChart, WorkoutProperty.DURATION.getUnit(context, combinedData.getYMax()));
        } catch (NoDataException e) {
            durationChart.clear();
        }
        durationChart.invalidate();
    }

    private void updateExploreChart(List<WorkoutType> workoutTypes) {
        WorkoutProperty property = WorkoutProperty.getById(exploreTitle.getSelectedItemPosition());
        CombinedData combinedData = new CombinedData();
        String lowest, highest;

        try {
            if (exploreChartSwitch.isChecked()) {
                BarDataSet barDataSet = statsProvider.getSumData(aggregationSpan, workoutTypes, property);
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
            exploreChart.clear();
            ((CombinedChartRenderer) exploreChart.getRenderer()).createRenderers();
            if(!(property == WorkoutProperty.START) && !(property == WorkoutProperty.END)) {
                String unit = property.getUnit(getContext(), combinedData.getYMax() - combinedData.getYMin());
                ChartStyles.setYAxisLabel(exploreChart, unit);
            }
            ChartStyles.updateStatsHistoryCombinedChartToSpan(exploreChart, combinedData, aggregationSpan, getContext());

        } catch (NoDataException e) {
            exploreChart.clear();
        }
        exploreChart.invalidate();
    }


    @Override
    public String getTitle() {
        return context.getString(R.string.stats_history_title);
    }
}