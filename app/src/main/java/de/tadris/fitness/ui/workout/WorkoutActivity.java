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

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.Interval;
import de.tadris.fitness.data.IntervalSet;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutData;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.map.GradientColoringStrategy;
import de.tadris.fitness.map.ColoringStrategy;
import de.tadris.fitness.map.MapManager;
import de.tadris.fitness.map.WorkoutLayer;
import de.tadris.fitness.ui.workout.diagram.SampleConverter;
import de.tadris.fitness.util.WorkoutCalculator;
import de.tadris.fitness.util.unit.DistanceUnitUtils;
import de.tadris.fitness.util.unit.EnergyUnitUtils;

public abstract class WorkoutActivity extends InformationActivity {

    public static final String WORKOUT_ID_EXTRA = "de.tadris.fitness.WorkoutActivity.WORKOUT_ID_EXTRA";

    List<WorkoutSample> samples;
    Workout workout;
    private Resources.Theme theme;
    MapView mapView;
    private FixedPixelCircle highlightingCircle;
    final Handler mHandler = new Handler();
    protected IntervalSet usedIntervalSet;
    protected Interval[] intervals;

    protected DistanceUnitUtils distanceUnitUtils;
    protected EnergyUnitUtils energyUnitUtils;

    void initBeforeContent() {
        distanceUnitUtils = Instance.getInstance(this).distanceUnitUtils;
        energyUnitUtils = Instance.getInstance(this).energyUnitUtils;

        Intent intent = getIntent();
        long workoutId = intent.getLongExtra(WORKOUT_ID_EXTRA, 0);
        if (workoutId != 0) {
            workout = Instance.getInstance(this).db.workoutDao().getWorkoutById(workoutId);
        }
        if (workout == null) {
            Toast.makeText(this, R.string.cannotFindWorkout, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        samples = Arrays.asList(Instance.getInstance(this).db.workoutDao().getAllSamplesOfWorkout(workout.id));
        if (workout.intervalSetUsedId != 0) {
            usedIntervalSet = Instance.getInstance(this).db.intervalDao().getSet(workout.intervalSetUsedId);
            intervals = Instance.getInstance(this).db.intervalDao().getAllIntervalsOfSet(usedIntervalSet.id);
        }
        setTheme(Instance.getInstance(this).themes.getWorkoutTypeTheme(workout.getWorkoutType(this)));
    }

    void initAfterContent() {
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(workout.getWorkoutType(this).title);

        theme = getTheme();
    }

    protected CombinedChart addDiagram(SampleConverter converter) {
        CombinedChart chart = getDiagram(converter);
        root.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fullScreenItems ? ViewGroup.LayoutParams.MATCH_PARENT : getMapHeight() / 2));
        return chart;
    }

    boolean diagramsInteractive = false;

    private CombinedChart getDiagram(SampleConverter converter) {
        return getDiagram(Collections.singletonList(converter), converter.isIntervalSetVisible());
    }

    private CombinedChart getDiagram(List<SampleConverter> converters, boolean showIntervalSets) {
        CombinedChart chart = new CombinedChart(this);

        chart.setScaleXEnabled(diagramsInteractive);
        chart.setScaleYEnabled(false);
        if (diagramsInteractive) {
            chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    onNothingSelected();
                    WorkoutSample sample = findSample(e);
                    if (sample != null) {
                        onDiagramValueSelected(sample.toLatLong());
                    }
                }

                @Override
                public void onNothingSelected() {
                    if(highlightingCircle != null){
                        mapView.getLayerManager().getLayers().remove(highlightingCircle);
                    }
                }
            });
        }
        chart.invalidate();

        chart.getAxisLeft().setTextColor(getThemeTextColor());
        chart.getAxisRight().setTextColor(getThemeTextColor());
        chart.getXAxis().setTextColor(getThemeTextColor());
        chart.getLegend().setTextColor(getThemeTextColor());
        chart.getDescription().setTextColor(getThemeTextColor());


        updateChart(chart, converters, showIntervalSets);

        for (SampleConverter converter : converters) {
            converter.afterAdd(chart);
        }

        return chart;
    }

    protected void updateChart(CombinedChart chart, List<SampleConverter> converters, boolean showIntervalSets) {
        boolean hasMultipleConverters = converters.size() > 1;
        CombinedData combinedData = new CombinedData();

        Description description = new Description();

        if (hasMultipleConverters || converters.size() == 0) {
            description.setText("");
        } else {
            description.setText(converters.get(0).getDescription());
        }
        chart.setDescription(description);
        chart.getAxisLeft().setValueFormatter(null);
        chart.getAxisRight().setValueFormatter(null);

        LineData lineData = new LineData();

        int converterIndex = 0;
        for (SampleConverter converter : converters) {
            converter.onCreate(getWorkoutData());

            List<Entry> entries = new ArrayList<>();
            for (WorkoutSample sample : samples) {
                // turn data into Entry objects
                Entry e = new Entry((float) (sample.relativeTime) / 1000f / 60f, converter.getValue(sample), sample);
                entries.add(e);
            }

            LineDataSet dataSet = new LineDataSet(entries, converter.getName()); // add entries to dataset
            int color = hasMultipleConverters ? getResources().getColor(converter.getColor()) : getThemePrimaryColor();
            dataSet.setColor(color);
            dataSet.setValueTextColor(color);
            dataSet.setDrawCircles(false);
            dataSet.setLineWidth(4);
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            if (converters.size() == 2) {
                YAxis.AxisDependency axisDependency = converterIndex == 0 ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT;
                dataSet.setAxisDependency(axisDependency);
                chart.getAxis(axisDependency).setValueFormatter(new DefaultAxisValueFormatter(0) {
                    @Override
                    public String getFormattedValue(float value) {
                        return super.getFormattedValue(value) + " " + converter.getUnit();
                    }
                });
            }
            lineData.addDataSet(dataSet);
            converterIndex++;
        }

        combinedData.setData(lineData);

        float yMax = lineData.getDataSetByIndex(0).getYMax() * 1.05f;
        if (showIntervalSets && intervals != null && intervals.length > 0) {
            List<BarEntry> barEntries = new ArrayList<>();

            for (long relativeTime : WorkoutCalculator.getIntervalSetTimesFromWorkout(getWorkoutData(), intervals)) {
                barEntries.add(new BarEntry((float) (relativeTime) / 1000f / 60f, yMax));
            }

            BarDataSet barDataSet = new BarDataSet(barEntries, getString(R.string.intervalSet));
            barDataSet.setBarBorderWidth(3);
            barDataSet.setBarBorderColor(getThemePrimaryColor());
            barDataSet.setColor(getThemePrimaryColor());

            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.01f);
            barData.setDrawValues(false);

            combinedData.setData(barData);
        } else {
            combinedData.setData(new BarData()); // Empty bar data
        }

        chart.setData(combinedData);
        chart.invalidate();
    }

    private void onDiagramValueSelected(LatLong latLong) {
        Paint p = AndroidGraphicFactory.INSTANCE.createPaint();
        p.setColor(0xff693cff);
        highlightingCircle = new FixedPixelCircle(latLong, 10, p, null);
        mapView.addLayer(highlightingCircle);

        if (!mapView.getBoundingBox().contains(latLong)) {
            mapView.getModel().mapViewPosition.animateTo(latLong);
        }
    }

    private WorkoutSample findSample(Entry entry) {
        if (entry.getData() instanceof WorkoutSample) {
            return (WorkoutSample) entry.getData();
        } else {
            return null;
        }
    }

    protected boolean showPauses = false;
    boolean fullScreenItems = false;
    LinearLayout mapRoot;

    static int myCounter =1;
    void addMap(){
        mapView = MapManager.setupMap(this);
        String trackStyle = Instance.getInstance(mapView.getContext()).userPreferences.getTrackStyle();
        // emulate current behaviour
        WorkoutLayer workoutLayer;


        ColoringStrategy coloringStrategy;

        // predefined set of settings that play with the colors, the mapping of the color to some
        // value and whether to blend or not. In the future it would be nice to have a nice editor
        // in the settings to tweak the numbers here and possibly create good looking colors.
        if(trackStyle.equals("theme_alpha")){
            /* use theme color but with alpha */
            int[] c1 = { (getThemePrimaryColor() & 0xffffff) ^ 0x55000000, getThemePrimaryColor()};
            coloringStrategy = new GradientColoringStrategy(c1, 0, workout.topSpeed, true);
        } else  if(trackStyle.equals("purple_rain")){
            /* a nice set of colors generated from colorbrewer */
            coloringStrategy= GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_PURPLE, workout.avgSpeed * 0.8, workout.avgSpeed * 1.2, true);
        } else if (trackStyle.equals("pink_mist")){
            /* Pink is nice */
            coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_PINK, 0 , workout.topSpeed, false);
        } else if (trackStyle.equals("rainbow_warrior")){
            /* Attempt to use different colors, this would be best suited for a fixed scale e.g. green is target value , red is to fast , yellow it to slow */
            coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_MAP, workout.avgSpeed * 0.6, workout.avgSpeed * 1.4, true);
        } else if (trackStyle.equals("bright_night")){
            coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_BRIGHT, 0 , workout.topSpeed, false);
        } else if (trackStyle.equals("mondriaan")){
            coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_YELLOW_RED_BLUE, workout.avgSpeed * 0.98, workout.avgSpeed * 1.02, false);
        } else {// theme_color
            /* default: original color based on theme*/
            int[] c2 = {getThemePrimaryColor() , getThemePrimaryColor()};
            coloringStrategy = new GradientColoringStrategy(c2, 0, workout.topSpeed, false);
        }

        workoutLayer = new WorkoutLayer(samples, coloringStrategy);
        mapView.addLayer(workoutLayer);

        final BoundingBox bounds= workoutLayer.getBoundingBox().extendMeters(50);
        mHandler.postDelayed(() -> {
            mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(bounds.getCenterPoint(),
                                                                              (LatLongUtils.zoomForBounds(mapView.getDimension(), bounds,
                                                                                                          mapView.getModel().displayModel.getTileSize()))));
            mapView.animate().alpha(1f).setDuration(1000).start();
        }, 1000);

        mapRoot = new LinearLayout(this);
        mapRoot.setOrientation(LinearLayout.VERTICAL);
        mapRoot.addView(mapView);

        root.addView(mapRoot, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                         fullScreenItems ? ViewGroup.LayoutParams.MATCH_PARENT : getMapHeight()));
        mapView.setAlpha(0);

        if(showPauses){
            Paint pBlue = AndroidGraphicFactory.INSTANCE.createPaint();
            pBlue.setColor(Color.BLUE);
            for (WorkoutCalculator.Pause pause : WorkoutCalculator.getPausesFromWorkout(getWorkoutData())) {
                float radius = Math.min(10, Math.max(2, (float) Math.sqrt((float) pause.duration / 1000)));
                mapView.addLayer(new FixedPixelCircle(pause.location, radius, pBlue, null));
            }
        }

        Paint pGreen = AndroidGraphicFactory.INSTANCE.createPaint();
        pGreen.setColor(Color.GREEN);
        mapView.addLayer(new FixedPixelCircle(samples.get(0).toLatLong(), 10, pGreen, null));

        Paint pRed = AndroidGraphicFactory.INSTANCE.createPaint();
        pRed.setColor(Color.RED);
        mapView.addLayer(new FixedPixelCircle(samples.get(samples.size() - 1).toLatLong(), 10, pRed, null));

        mapView.setClickable(false);

    }

    private int getMapHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels * 3 / 4;
    }

    protected boolean hasSamples() {
        return samples.size() > 1;
    }

    protected WorkoutData getWorkoutData() {
        return new WorkoutData(workout, samples);
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            mapView.destroyAll();
        }
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            for (Layer layer : mapView.getLayerManager().getLayers()) {
                if (layer instanceof TileDownloadLayer) {
                    ((TileDownloadLayer) layer).onPause();
                }
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (mapView != null) {
            for (Layer layer : mapView.getLayerManager().getLayers()) {
                if (layer instanceof TileDownloadLayer) {
                    ((TileDownloadLayer) layer).onResume();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
