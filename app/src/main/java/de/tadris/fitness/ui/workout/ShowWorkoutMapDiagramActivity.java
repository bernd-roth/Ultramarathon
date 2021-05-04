/*
 * Copyright (c) 2021 Jannis Scheibe <jannis@tadris.de>
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

package de.tadris.fitness.ui.workout;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.highlight.Highlight;

import java.util.HashMap;
import java.util.Map;

import de.tadris.fitness.R;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.ui.dialog.SampleConverterPickerDialog;
import de.tadris.fitness.ui.workout.diagram.ConverterManager;
import de.tadris.fitness.ui.workout.diagram.HeartRateConverter;
import de.tadris.fitness.ui.workout.diagram.HeightConverter;
import de.tadris.fitness.ui.workout.diagram.SampleConverter;
import de.tadris.fitness.ui.workout.diagram.SpeedConverter;

public class ShowWorkoutMapDiagramActivity extends WorkoutActivity {

    public static final String DIAGRAM_TYPE_EXTRA = "de.tadris.fitness.ShowWorkoutMapDiagramActivity.DIAGRAM_TYPE";

    public static final String DIAGRAM_TYPE_HEIGHT = "height";
    public static final String DIAGRAM_TYPE_SPEED = "speed";
    public static final String DIAGRAM_TYPE_HEART_RATE = "heartrate";

    private ConverterManager converterManager;

    private CombinedChart chart;
    private TextView selection;
    private CheckBox showIntervals;

    private MenuItem autoColoring, noColoring;
    private Map<MenuItem, SampleConverter> converterMenu = new HashMap<>();

    private static final int COLORING_PROPERTY_AUTO = 0;
    private static final int COLORING_PROPERTY_NONE = 1;
    private static final int COLORING_PROPERTY_CUSTOM = 2;

    private SampleConverter coloringConverter;
    private int coloringPropertyMode = COLORING_PROPERTY_AUTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBeforeContent();

        converterManager = new ConverterManager(this, getWorkoutData());

        setContentView(R.layout.activity_show_workout_map_diagram);
        initRoot();

        this.selection = findViewById(R.id.showWorkoutDiagramInfo);
        this.showIntervals = findViewById(R.id.showWorkoutDiagramIntervals);

        initAfterContent();

        fullScreenItems = true;
        addMap();

        mapView.setClickable(true);

        diagramsInteractive = true;
        root = findViewById(R.id.showWorkoutDiagramParent);

        initDiagram();

        findViewById(R.id.showWorkoutDiagramSelector).setOnClickListener(v -> new SampleConverterPickerDialog(this, this::updateChart, converterManager).show());
        showIntervals.setOnCheckedChangeListener((buttonView, isChecked) -> updateChart());
        showIntervals.setVisibility(intervals != null && intervals.length > 0 ? View.VISIBLE : View.GONE);

        refreshColoring();
    }

    @Override
    public void onSelectionChanged(WorkoutSample sample) {

        if (sample == null) {
            chart.highlightValue(null);
        } else {
            float dataIndex = (sample.relativeTime) / 1000f / 60f;
            Highlight h = new Highlight((float) dataIndex, 0, -1);
            h.setDataIndex(0);
            chart.highlightValue(h);
            chart.centerViewTo(dataIndex,0, YAxis.AxisDependency.LEFT);
        }
        onChartSelectionChanged(sample);
    }

    private void initDiagram() {
        SampleConverter defaultConverter = getDefaultConverter();
        converterManager.selectedConverters.add(defaultConverter);
        chart = addDiagram(defaultConverter);
        updateChart();
    }

    private SampleConverter getDefaultConverter() {
        String typeExtra = getIntent().getStringExtra(DIAGRAM_TYPE_EXTRA);
        if (typeExtra == null) typeExtra = "";
        switch (typeExtra) {
            default:
            case DIAGRAM_TYPE_SPEED:
                return new SpeedConverter(this);
            case DIAGRAM_TYPE_HEIGHT:
                return new HeightConverter(this);
            case DIAGRAM_TYPE_HEART_RATE:
                return new HeartRateConverter(this);
        }
    }

    private void updateChart() {
        updateChart(chart, converterManager.selectedConverters, showIntervals.isChecked());
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (SampleConverter converter : converterManager.selectedConverters) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(converter.getName());
        }
        selection.setText(converterManager.selectedConverters.size() > 0 ? sb.toString() : getString(R.string.nothingSelected));
        refreshColoring();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.workout_map_menu, menu);

        SubMenu coloringMenu = menu.findItem(R.id.actionSelectColoring).getSubMenu();

        autoColoring = coloringMenu.add(R.string.auto);
        noColoring = coloringMenu.add(R.string.noColoring);
        for (int i = 0; i < converterManager.availableConverters.size(); i++) {
            SampleConverter converter = converterManager.availableConverters.get(i);
            MenuItem item = coloringMenu.add(R.id.actionSelectColoring, Menu.NONE, i, converter.getName());
            converterMenu.put(item, converter);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == autoColoring) {
            coloringPropertyMode = COLORING_PROPERTY_AUTO;
            refreshColoring();
            return true;
        } else if (item == noColoring) {
            coloringPropertyMode = COLORING_PROPERTY_NONE;
            refreshColoring();
            return true;
        } else if (converterMenu.containsKey(item)) {
            coloringPropertyMode = COLORING_PROPERTY_CUSTOM;
            coloringConverter = converterMenu.get(item);
            refreshColoring();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshColoring() {
        SampleConverter converter = null;
        if (coloringPropertyMode == COLORING_PROPERTY_AUTO) {
            converter = converterManager.selectedConverters.get(0);
        } else if (coloringPropertyMode == COLORING_PROPERTY_CUSTOM) {
            converter = coloringConverter;
        }
        if (converter != null) {
            converter.onCreate(getWorkoutData());
        }
        workoutLayer.setSampleConverter(workout, converter);
    }

    @Override
    protected void initRoot() {
        root = findViewById(R.id.showWorkoutMapParent);
    }
}