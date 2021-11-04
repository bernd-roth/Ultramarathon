package de.tadris.fitness.ui.statistics.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.animation.Easing;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.data.StatsDataProvider;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.data.WorkoutTypeManager;
import de.tadris.fitness.ui.dialog.SelectWorkoutTypeDialog;
import de.tadris.fitness.ui.statistics.DetailStatsActivity;
import de.tadris.fitness.ui.statistics.WorkoutTypeSelection;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.charts.DataSetStyles;
import de.tadris.fitness.util.charts.formatter.FractionedDateFormatter;
import de.tadris.fitness.util.charts.formatter.TimeFormatter;
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

    TextView pauseDurationTitle;
    Switch pauseDurationSwitch;
    CombinedChart pauseDurationChart;

    TextView distanceTitle;
    Switch distanceSwitch;
    CombinedChart distanceChart;

    float stats_time_factor;

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
        TypedValue stats_time_factor = new TypedValue();
        context.getResources().getValue(R.dimen.stats_time_factor, stats_time_factor, true);
        this.stats_time_factor = stats_time_factor.getFloat();
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
        speedSwitch = view.findViewById(R.id.speed_switch);
        speedSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (speedSwitch.isChecked()) {
                    speedTitle.setText(R.string.workoutPace);
                } else {
                    speedTitle.setText(R.string.workoutSpeed);
                }
                updateSpeedChart(selection.getSelectedWorkoutTypes());
            }
        });

        distanceTitle = view.findViewById(R.id.stats_history_distance_title);
        distanceChart = view.findViewById(R.id.stats_history_distance_chart);
        distanceSwitch = view.findViewById(R.id.distance_switch);
        distanceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (distanceSwitch.isChecked()) {
                    distanceTitle.setText(R.string.workoutDistanceSum);
                } else {
                    distanceTitle.setText(R.string.workoutAvgDistance);
                }
                updateDistanceChart(selection.getSelectedWorkoutTypes());
            }
        });

        durationTitle = view.findViewById(R.id.stats_history_duration_title);
        durationChart = view.findViewById(R.id.stats_duration_chart);
        durationSwitch = view.findViewById(R.id.duration_switch);
        durationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (durationSwitch.isChecked()) {
                    durationTitle.setText(R.string.workoutDurationSum);
                } else {
                    durationTitle.setText(R.string.workoutAvgDurationLong);
                }
                updateDurationChart(selection.getSelectedWorkoutTypes());
            }
        });

        pauseDurationTitle = view.findViewById(R.id.stats_history_pause_duration_title);
        pauseDurationChart = view.findViewById(R.id.stats_pause_duration_chart);
        pauseDurationSwitch = view.findViewById(R.id.pause_duration_switch);
        pauseDurationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pauseDurationSwitch.isChecked()) {
                    pauseDurationTitle.setText(R.string.workoutPauseDurationSum);
                } else {
                    pauseDurationTitle.setText(R.string.workoutAvgPauseDuration);
                }
                updatePauseDurationChart(selection.getSelectedWorkoutTypes());
            }
        });


        combinedChartList.add(speedChart);
        combinedChartList.add(distanceChart);
        combinedChartList.add(durationChart);
        combinedChartList.add(pauseDurationChart);

        for (CombinedChart combinedChart : combinedChartList) {
            animateChart(combinedChart);
            fixViewPortOffsets(combinedChart, 120);
            ChartStyles.defaultLineChart(combinedChart);
            statsProvider.setXLimits(combinedChart);
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
                    i.putExtra("data", combinedChart.getData().getDataSetLabels()[0]);
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
                    float high = combinedChart.getHighestVisibleX();
                    float low= combinedChart.getLowestVisibleX();
                    long timeSpan = (long) ((high - low) * stats_time_factor);

                    AggregationSpan oldAggregationSpan = aggregationSpan;

                    if (TimeUnit.DAYS.toMillis(1095) < timeSpan) {
                        aggregationSpan = AggregationSpan.YEAR;
                    } else if (TimeUnit.DAYS.toMillis(93) < timeSpan) {
                        aggregationSpan = AggregationSpan.MONTH;
                    } else if (TimeUnit.DAYS.toMillis(21) < timeSpan) {
                        aggregationSpan = AggregationSpan.WEEK;
                    } else {
                        aggregationSpan = AggregationSpan.SINGLE;
                    }

                    if (oldAggregationSpan != aggregationSpan) {
                        updateCharts(selection.getSelectedWorkoutTypes());
                    }
                }

                @Override
                public void onChartTranslate(MotionEvent me, float dX, float dY) {

                }
            });
            combinedChart.setOnChartGestureListener(multiListener);
        }

        List<WorkoutType> selected = preferences.getStatisticsSelectedTypes();
        if (selected.get(0) == null)
            selected.clear();
        if(selected.size() == 0)
            selected.addAll(WorkoutTypeManager.getInstance().getAllTypes(context));
        selection.setSelectedWorkoutTypes(selected);

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
        updatePauseDurationChart(workoutTypes);
        updateDistanceChart(workoutTypes);
    }

    private void updateSpeedChart(List<WorkoutType> workoutTypes) {
        CandleDataSet candleDataSet;

        try {
            if (speedSwitch.isChecked()) {
                // Retrieve candle data
                candleDataSet = statsProvider.getPaceCandleData(aggregationSpan, workoutTypes);
                ChartStyles.setYAxisLabel(speedChart, Instance.getInstance(context).distanceUnitUtils.getPaceUnit());
            } else {
                candleDataSet = statsProvider.getSpeedCandleData(aggregationSpan, workoutTypes);
                ChartStyles.setYAxisLabel(speedChart, Instance.getInstance(context).distanceUnitUtils.getDistanceUnitSystem().getSpeedUnit());
            }

            // Add candle data
            CombinedData combinedData = new CombinedData();
            combinedData.setData(new CandleData(candleDataSet));

            // Create background line
            LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
            combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(context, lineDataSet)));

            updateChartToData(speedChart, combinedData);
        } catch (NoDataException e) {
            speedChart.clear();
        }
        speedChart.invalidate();
    }

    private void updateDistanceChart(List<WorkoutType> workoutTypes) {
        CombinedData combinedData = new CombinedData();

        try {
            if (distanceSwitch.isChecked()) {
                BarDataSet barDataSet = statsProvider.getDistanceSumData(aggregationSpan, workoutTypes);
                BarData barData = new BarData(barDataSet);
                ChartStyles.setTextAppearance(barData);
                barData.setBarWidth(aggregationSpan.spanInterval / stats_time_factor * ChartStyles.BAR_WIDTH_FACTOR);
                combinedData.setData(barData);
            } else {
                CandleDataSet candleDataSet = statsProvider.getDistanceCandleData(aggregationSpan, workoutTypes);
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
            updateChartToData(distanceChart, combinedData);
            ChartStyles.setYAxisLabel(distanceChart, Instance.getInstance(getContext()).distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit());
        } catch (NoDataException e) {
            distanceChart.clear();
        }
        distanceChart.invalidate();
    }

    private void fixViewPortOffsets(CombinedChart chart, float offset)
    {
        chart.setViewPortOffsets(offset, offset/2,offset/2,offset/2);
    }

    private void updateDurationChart(List<WorkoutType> workoutTypes) {
        CombinedData combinedData = new CombinedData();

        try {
            if (durationSwitch.isChecked()) {
                BarDataSet barDataSet = statsProvider.getDurationSumData(aggregationSpan, workoutTypes);
                BarData barData = new BarData(barDataSet);
                ChartStyles.setTextAppearance(barData);
                barData.setBarWidth(aggregationSpan.spanInterval / stats_time_factor * ChartStyles.BAR_WIDTH_FACTOR);
                combinedData.setData(barData);
            } else {
                CandleDataSet candleDataSet = statsProvider.getDurationCandleData(aggregationSpan, workoutTypes);
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
            updateChartToData(durationChart, combinedData);
            ChartStyles.setYAxisLabel(durationChart, ((TimeFormatter)durationChart.getAxisLeft().getValueFormatter()).getUnit(getContext()));
        } catch (NoDataException e) {
            durationChart.clear();
        }
        durationChart.invalidate();
    }

    private void updatePauseDurationChart(List<WorkoutType> workoutTypes) {
        CombinedData combinedData = new CombinedData();

        try {
            if (pauseDurationSwitch.isChecked()) {
                BarDataSet barDataSet = statsProvider.getPauseDurationSumData(aggregationSpan, workoutTypes);
                BarData barData = new BarData(barDataSet);
                ChartStyles.setTextAppearance(barData);
                barData.setBarWidth(aggregationSpan.spanInterval / stats_time_factor * ChartStyles.BAR_WIDTH_FACTOR);
                combinedData.setData(barData);
            } else {
                CandleDataSet candleDataSet = statsProvider.getPauseDurationCandleData(aggregationSpan, workoutTypes);
                combinedData.setData(new CandleData(candleDataSet));
                // Create background line
                LineDataSet lineDataSet = StatsProvider.convertCandleToMeanLineData(candleDataSet);
                combinedData.setData(new LineData(DataSetStyles.applyBackgroundLineStyle(context, lineDataSet)));
            }

            // It is very dumb but CombinedChart.setData() calls the initBuffer method of all renderer before resetting the renderer (because the super call is executed before).
            // In case a bar chart was displayed before but not longer, the activity would crash.
            // Therefore the following two lines resets all renderers manually.
            pauseDurationChart.clear();
            ((CombinedChartRenderer) pauseDurationChart.getRenderer()).createRenderers();
            updateChartToData(pauseDurationChart, combinedData);
            ChartStyles.setYAxisLabel(pauseDurationChart, ((TimeFormatter)combinedData.getMaxEntryCountSet().getValueFormatter()).getUnit(getContext()));
        } catch (NoDataException e) {
            pauseDurationChart.clear();
        }
        pauseDurationChart.invalidate();
    }

    private void updateChartToData(CombinedChart chart, CombinedData data)
    {
        chart.setData(data);
        chart.getXAxis().setValueFormatter(new FractionedDateFormatter(context,aggregationSpan));
        chart.getXAxis().setGranularity((float)aggregationSpan.spanInterval / stats_time_factor);
        chart.getAxisLeft().setValueFormatter(data.getMaxEntryCountSet().getValueFormatter());
        ChartStyles.setXAxisLabel(chart, getString(aggregationSpan.axisLabel));
        chart.getXAxis().setAxisMinimum(data.getXMin()-aggregationSpan.spanInterval/stats_time_factor/2);
        chart.getXAxis().setAxisMaximum(data.getXMax()+aggregationSpan.spanInterval/stats_time_factor/2);
    }

    private void animateChart (CombinedChart chart) {
        chart.animateY(500, Easing.EaseInExpo);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_history_title);
    }
}