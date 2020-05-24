/*
 * Copyright (c) 2020 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tadris.fitness.activity.workout;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.activity.FitoTrackActivity;
import de.tadris.fitness.aggregation.AggregatedInformationDataPoint;
import de.tadris.fitness.aggregation.AggregatedWorkoutData;
import de.tadris.fitness.aggregation.AggregationSpan;
import de.tadris.fitness.aggregation.AggregationType;
import de.tadris.fitness.aggregation.WorkoutAggregator;
import de.tadris.fitness.aggregation.WorkoutInformation;
import de.tadris.fitness.aggregation.WorkoutInformationManager;
import de.tadris.fitness.aggregation.WorkoutTypeFilter;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.dialog.SelectWorkoutTypeDialog;
import de.tadris.fitness.util.unit.UnitUtils;

import static android.widget.AdapterView.OnItemSelectedListener;

public class ShowWorkoutsAggregatedDiagramActivity extends FitoTrackActivity implements SelectWorkoutTypeDialog.WorkoutTypeSelectListener {

    LineChart chart;
    Spinner informationSelector, timeSpanSelector;
    View typeSelector;
    TextView infoMin, infoAvg, infoMax;
    TextView workoutTypeText;
    TextView axisLeftLabel, axisRightLabel, xAxisLabel;
    ImageView workoutTypeIcon;
    WorkoutInformationManager informationManager;

    WorkoutInformation selectedInformation;
    WorkoutType selectedWorkoutType = WorkoutType.RUNNING;
    AggregationSpan selectedSpan = AggregationSpan.WEEK;

    AggregatedWorkoutData aggregatedWorkoutData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workouts_aggregated);
        setTitle(getString(R.string.workout_statistics));
        setupActionBar();

        informationSelector = findViewById(R.id.aggregationInfo);
        typeSelector = findViewById(R.id.aggregationWorkoutTypeSelector);
        timeSpanSelector = findViewById(R.id.aggregationSpan);
        infoMin = findViewById(R.id.aggregationOverviewMin);
        infoAvg = findViewById(R.id.aggregationOverviewAvg);
        infoMax = findViewById(R.id.aggregationOverviewMax);
        axisLeftLabel = findViewById(R.id.aggregationDiagramLeftAxis);
        axisRightLabel = findViewById(R.id.aggregationDiagramRightAxis);
        xAxisLabel = findViewById(R.id.aggregationDiagramXAxis);
        workoutTypeText = findViewById(R.id.aggregationWorkoutTypeTitle);
        workoutTypeIcon = findViewById(R.id.aggregationWorkoutTypeIcon);
        chart = findViewById(R.id.aggregationChart);

        informationManager = new WorkoutInformationManager(this);
        selectedInformation = informationManager.getInformation().get(0);

        initInformationSpinner();
        initTypeSelector();
        initTimeSpanSpinner();

        initChart();

        refresh();

        timeSpanSelector.setSelection(2);
        onSelectWorkoutType(Instance.getInstance(this).db.workoutDao().getLastWorkout().getWorkoutType());
    }

    private void initInformationSpinner() {
        List<WorkoutInformation> informationList = informationManager.getInformation();
        List<String> strings = new ArrayList<>();
        for (WorkoutInformation information : informationList) {
            strings.add(getString(information.getTitleRes()));
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strings);
        informationSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                onWorkoutInformationSelect(informationList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        informationSelector.setAdapter(spinnerAdapter);
    }

    private void initTypeSelector() {
        typeSelector.setOnClickListener(v -> {
            new SelectWorkoutTypeDialog(this, this).show();
        });
    }

    private void initTimeSpanSpinner() {
        AggregationSpan[] spans = AggregationSpan.values();
        List<String> strings = new ArrayList<>();
        for (AggregationSpan span : spans) {
            strings.add(getString(span.title));
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strings);
        timeSpanSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                onTimeSpanSelect(spans[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpanSelector.setAdapter(spinnerAdapter);
    }

    private void initChart() {
        chart.getAxisLeft().setTextColor(getThemeTextColor());
        chart.getAxisLeft().setTextSize(12);
        chart.getAxisRight().setTextColor(getThemeTextColor());
        chart.getAxisRight().setTextSize(12);

        chart.getXAxis().setTextColor(getThemeTextColor());
        chart.getXAxis().setTextSize(12);
        chart.getXAxis().setYOffset(-1);

        chart.setNoDataText(getString(R.string.no_workouts_recorded_for_this_activity));
        chart.setNoDataTextColor(getThemeTextColor());

        chart.setScaleYEnabled(false);

        chart.getLegend().setTextColor(getThemeTextColor());
        chart.getLegend().setTextSize(12);
    }

    private void refresh() {
        aggregatedWorkoutData = new WorkoutAggregator(this, new WorkoutTypeFilter(selectedWorkoutType), selectedInformation, selectedSpan).aggregate();
        refreshValueTexts();
        refreshChart();
        setTitle(getString(selectedInformation.getTitleRes()) + " " + getString(R.string.per) + " " + getString(selectedSpan.title));
        workoutTypeText.setText(getString(selectedWorkoutType.title));
        workoutTypeIcon.setImageResource(selectedWorkoutType.icon);
        axisLeftLabel.setText(selectedInformation.getUnit());
        axisRightLabel.setText(selectedInformation.getUnit());
        xAxisLabel.setText(selectedSpan.axisLabel);
    }

    private void refreshValueTexts() {
        String unitSuffix = " " + selectedInformation.getUnit();
        infoMin.setText(getString(R.string.min) + ": " + UnitUtils.round(aggregatedWorkoutData.getMin(), 2) + unitSuffix);
        if (selectedInformation.getAggregationType() == AggregationType.SUM) {
            infoAvg.setText(getString(R.string.sum) + ": " + UnitUtils.round(aggregatedWorkoutData.getSum(), 2) + unitSuffix);
        } else {
            infoAvg.setText(getString(R.string.avg) + ": " + UnitUtils.round(aggregatedWorkoutData.getAvg(), 2) + unitSuffix);
        }
        infoMax.setText(getString(R.string.max) + ": " + UnitUtils.round(aggregatedWorkoutData.getMax(), 2) + unitSuffix);
    }

    private void refreshChart() {
        chart.resetTracking();
        chart.resetZoom();
        chart.resetViewPortOffsets();
        chart.clear();

        if (aggregatedWorkoutData.getDataPoints().size() == 0) {
            return;
        }

        LineData lineData = createLineData();
        chart.setData(lineData);

        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return selectedSpan.dateFormat.format(new Date((long) value + (selectedSpan.spanInterval / 2)));
            }
        });
        chart.getXAxis().setGranularity(selectedSpan.spanInterval);

        Description description = new Description();
        description.setTextColor(getThemeTextColor());
        description.setText(selectedInformation.getUnit());
        description.setTextSize(12);
        chart.setDescription(description);

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e.getData() != null && e.getData() instanceof AggregatedInformationDataPoint) {
                    AggregatedInformationDataPoint dataPoint = (AggregatedInformationDataPoint) e.getData();
                    String formattedDate = chart.getXAxis().getValueFormatter().getFormattedValue(dataPoint.getDate().getTime());
                    String text = getString(selectedSpan.title) + " " + formattedDate + ": " + UnitUtils.round(e.getY(), 2) + " " + selectedInformation.getUnit();
                    chart.getDescription().setText(text);
                } else {
                    onNothingSelected();
                }
            }

            @Override
            public void onNothingSelected() {
                chart.getDescription().setText(selectedInformation.getUnit());
            }
        });

        chart.invalidate();
        chart.animateY(500, Easing.EaseOutCubic);
    }

    private LineData createLineData() {
        final ArrayList<Entry> entries = new ArrayList<>();
        for (AggregatedInformationDataPoint dataPoint : aggregatedWorkoutData.getDataPoints()) {
            float value;
            switch (selectedInformation.getAggregationType()) {
                default:
                case SUM:
                    value = (float) dataPoint.getSum();
                    break;
                case AVERAGE:
                    value = (float) dataPoint.getAvg();
                    break;
            }
            entries.add(new Entry(dataPoint.getDate().getTime(), value, dataPoint));
        }

        LineDataSet lineDataSet;
        lineDataSet = new LineDataSet(entries, getString(selectedInformation.getTitleRes()) + " - " + getString(selectedInformation.getAggregationType().title));
        lineDataSet.setColor(getThemePrimaryColor());
        lineDataSet.setValueTextColor(getThemeTextColor());
        lineDataSet.setValueTextSize(12);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setCircleColor(getThemePrimaryColor());
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleHoleRadius(2);
        lineDataSet.setCircleHoleColor(getThemeTextColorInverse());
        lineDataSet.setLineWidth(4);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        lineDataSet.setDrawValues(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        if (selectedInformation.getAggregationType() == AggregationType.AVERAGE
                || selectedSpan == AggregationSpan.SINGLE) {
            float xMin = lineDataSet.getXMin();
            float xMax = lineDataSet.getXMax();

            dataSets.add(createHorizontalLineData(xMin, xMax, (float) aggregatedWorkoutData.getMax(), getResources().getColor(R.color.aggregatedDiagramMax), getString(R.string.max)));
            dataSets.add(createHorizontalLineData(xMin, xMax, (float) aggregatedWorkoutData.getAvg(), getResources().getColor(R.color.aggregatedDiagramAvg), getString(R.string.avg)));
            dataSets.add(createHorizontalLineData(xMin, xMax, (float) aggregatedWorkoutData.getMin(), getResources().getColor(R.color.aggregatedDiagramMin), getString(R.string.min)));
        }

        return new LineData(dataSets);
    }

    private LineDataSet createHorizontalLineData(float xMin, float xMax, float y, int color, String label) {
        final ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(xMin, y));
        entries.add(new Entry(xMax, y));

        LineDataSet lineDataSet = new LineDataSet(entries, label);
        lineDataSet.setColor(color);
        lineDataSet.setLineWidth(2);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.enableDashedLine(10, 10, 0);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        return lineDataSet;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.workout_stats_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionStatsHelp) {
            showHelpDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.help)
                .setMessage(R.string.workoutStatsHelpText)
                .setPositiveButton(R.string.okay, null)
                .create().show();
    }

    @Override
    public void onSelectWorkoutType(WorkoutType workoutType) {
        selectedWorkoutType = workoutType;
        refresh();
    }

    private void onWorkoutInformationSelect(WorkoutInformation information) {
        this.selectedInformation = information;
        refresh();
    }

    private void onTimeSpanSelect(AggregationSpan span) {
        this.selectedSpan = span;
        refresh();
    }
}