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

package de.tadris.fitness.ui.workout;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;

import de.tadris.fitness.R;
import de.tadris.fitness.ui.dialog.SampleConverterPickerDialog;
import de.tadris.fitness.ui.workout.diagram.ConverterManager;
import de.tadris.fitness.ui.workout.diagram.SampleConverter;

public class ShowWorkoutMapDiagramActivity extends WorkoutActivity {

    public static final String DIAGRAM_TYPE_EXTRA = "de.tadris.fitness.ShowWorkoutMapDiagramActivity.DIAGRAM_TYPE";

    public static final String DIAGRAM_TYPE_HEIGHT = "height";
    public static final String DIAGRAM_TYPE_SPEED = "speed";

    private ConverterManager converterManager;

    private CombinedChart chart;
    private TextView selection;
    private CheckBox showIntervals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBeforeContent();

        converterManager = new ConverterManager(this);

        setContentView(R.layout.activity_show_workout_map_diagram);
        initRoot();

        this.selection = findViewById(R.id.showWorkoutDiagramSelector);
        this.showIntervals = findViewById(R.id.showWorkoutDiagramIntervals);

        initAfterContent();

        fullScreenItems = true;
        addMap();
        map.setClickable(true);

        diagramsInteractive = true;
        root = findViewById(R.id.showWorkoutDiagramParent);

        initDiagram();

        selection.setOnClickListener(v -> new SampleConverterPickerDialog(this, this::updateChart, converterManager).show());
        showIntervals.setOnCheckedChangeListener((buttonView, isChecked) -> updateChart());
        showIntervals.setVisibility(intervals != null && intervals.length > 0 ? View.VISIBLE : View.GONE);
    }

    private void initDiagram() {
        String typeExtra = getIntent().getStringExtra(DIAGRAM_TYPE_EXTRA);
        boolean isHeightDiagram = typeExtra != null && typeExtra.equals(DIAGRAM_TYPE_HEIGHT);
        SampleConverter defaultConverter = converterManager.availableConverters.get(isHeightDiagram ? 1 : 0);
        converterManager.selectedConverters.add(defaultConverter);
        chart = addDiagram(defaultConverter);
        updateChart();
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
    }


    @Override
    void initRoot() {
        root = findViewById(R.id.showWorkoutMapParent);
    }
}