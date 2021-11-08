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

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.List;

import de.tadris.fitness.R;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.aggregation.WorkoutInformationManager;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.ui.statistics.TimeSpanSelection;
import de.tadris.fitness.ui.statistics.WorkoutTypeSelection;
import de.tadris.fitness.util.WorkoutProperty;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.exceptions.NoDataException;

public class StatsExploreFragment extends StatsFragment {
    StatsProvider statsProvider;
    Spinner spinnerY, spinnerX, spinnerSize;
    TimeSpanSelection timeSpanSelection;
    WorkoutTypeSelection workoutTypeSelection;
    CombinedChart chart;

    public StatsExploreFragment(Context ctx) {
        super(R.layout.fragment_stats_explore, ctx);
        statsProvider = new StatsProvider(ctx);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinnerX = view.findViewById(R.id.spinner_x_axis);
        spinnerY = view.findViewById(R.id.spinner_y_axis);
        spinnerSize = view.findViewById(R.id.spinner_size);
        chart = view.findViewById(R.id.explore_chart);

        ChartStyles.defaultChart(chart);
        chart.getLegend().setEnabled(true);

        timeSpanSelection = view.findViewById(R.id.time_span_selection_exp);
        timeSpanSelection.setForegroundColor(getContext().getColor(R.color.textDarkerWhite));
        timeSpanSelection.addOnTimeSpanSelectionListener((aggregationSpan, instance) -> updateChart());

        workoutTypeSelection = view.findViewById(R.id.workout_type_selector_exp);
        ((TextView)workoutTypeSelection.findViewById(R.id.view_workout_type_selection_text)).setTextColor(getContext().getColor(R.color.textDarkerWhite));
        workoutTypeSelection.addOnWorkoutTypeSelectListener(workoutType -> updateChart());

        addAdapterAndListenerToSpinner(spinnerX);
        addAdapterAndListenerToSpinner(spinnerY);
        addAdapterAndListenerToSpinner(spinnerSize);
        spinnerY.setSelection(2);
        spinnerX.setSelection(8);
        spinnerSize.setSelection(7);
    }

    private void addAdapterAndListenerToSpinner(Spinner spinner)
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, WorkoutProperty.getStringRepresentations(getContext()));
        spinner.setAdapter(spinnerAdapter);
    }

    private void updateChart()
    {
        WorkoutProperty x = WorkoutProperty.getById(spinnerX.getSelectedItemPosition());
        WorkoutProperty y = WorkoutProperty.getById(spinnerY.getSelectedItemPosition());
        WorkoutProperty bubbleSize = WorkoutProperty.getById(spinnerSize.getSelectedItemPosition());

//        statsProvider.setAxisLimits(chart.getXAxis(), x);
//        statsProvider.setAxisLimits(chart.getAxisLeft(), y);

        long start = timeSpanSelection.getSelectedInstance();
        StatsDataTypes.TimeSpan span = new StatsDataTypes.TimeSpan(start, timeSpanSelection.getSelectedAggregationSpan().getAggregationEnd(start));

        try {
            List<BubbleDataSet> bubbleSets = statsProvider.getExploreData(workoutTypeSelection.getSelectedWorkoutTypes(),
                    span,
                    timeSpanSelection.getSelectedAggregationSpan(),
                    x,
                    y,
                    bubbleSize);
            BubbleData bubbleData= new BubbleData();
            for(BubbleDataSet bubbles : bubbleSets) {
                bubbleData.addDataSet(bubbles);
            }
            CombinedData dataSet = new CombinedData();
            dataSet.setData(bubbleData);
            chart.setData(dataSet);
            chart.getXAxis().setValueFormatter(StatsProvider.getValueFormatter(x, getContext(), 1000, AggregationSpan.MONTH));
            chart.getAxisLeft().setValueFormatter(StatsProvider.getValueFormatter(y, getContext(), 1000, AggregationSpan.MONTH));
        } catch (NoDataException e) {
            e.printStackTrace();
        }
        chart.invalidate();
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_explore_title);
    }
}
