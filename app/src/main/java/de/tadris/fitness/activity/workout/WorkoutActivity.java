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

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.Interval;
import de.tadris.fitness.data.IntervalSet;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutManager;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.map.MapManager;
import de.tadris.fitness.map.WorkoutLayer;
import de.tadris.fitness.util.unit.DistanceUnitUtils;
import de.tadris.fitness.util.unit.EnergyUnitUtils;

public abstract class WorkoutActivity extends InformationActivity {

    public static Workout selectedWorkout;

    List<WorkoutSample> samples;
    Workout workout;
    private Resources.Theme theme;
    MapView map;
    private TileDownloadLayer downloadLayer;
    private FixedPixelCircle highlightingCircle;
    final Handler mHandler = new Handler();
    protected IntervalSet usedIntervalSet;
    protected Interval[] intervals;

    protected DistanceUnitUtils distanceUnitUtils;
    protected EnergyUnitUtils energyUnitUtils;

    CombinedChart speedDiagram;
    CombinedChart heightDiagram;

    void initBeforeContent() {
        distanceUnitUtils = Instance.getInstance(this).distanceUnitUtils;
        energyUnitUtils = Instance.getInstance(this).energyUnitUtils;
        workout= selectedWorkout;
        samples= Arrays.asList(Instance.getInstance(this).db.workoutDao().getAllSamplesOfWorkout(workout.id));
        if (workout.intervalSetUsedId != 0) {
            usedIntervalSet = Instance.getInstance(this).db.intervalDao().getSet(workout.intervalSetUsedId);
            intervals = Instance.getInstance(this).db.intervalDao().getAllIntervalsOfSet(usedIntervalSet.id);
        }
        setTheme(Instance.getInstance(this).themes.getWorkoutTypeTheme(workout.getWorkoutType()));
    }

    void initAfterContent() {
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(workout.getWorkoutType().title);

        theme= getTheme();
    }

    private void addDiagram(SampleConverter converter) {
        root.addView(getDiagram(converter), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fullScreenItems ? ViewGroup.LayoutParams.MATCH_PARENT : getWindowManager().getDefaultDisplay().getWidth()*3/4));
    }

    boolean diagramsInteractive = false;

    private CombinedChart getDiagram(SampleConverter converter) {
        CombinedChart chart = new CombinedChart(this);
        CombinedData combinedData = new CombinedData();

        converter.onCreate();

        List<Entry> entries = new ArrayList<>();
        for (WorkoutSample sample : samples) {
            // turn your data into Entry objects
            Entry e = new Entry((float) (sample.relativeTime) / 1000f / 60f, converter.getValue(sample), sample);
            entries.add(e);
        }

        LineDataSet dataSet = new LineDataSet(entries, converter.getName()); // add entries to dataset
        dataSet.setColor(getThemePrimaryColor());
        dataSet.setValueTextColor(getThemePrimaryColor());
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(4);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        Description description= new Description();
        description.setText(converter.getDescription());

        LineData lineData = new LineData(dataSet);
        combinedData.setData(lineData);

        float yMax = lineData.getYMax() * 1.05f;
        if (converter.isIntervalSetVisible() && intervals != null && intervals.length > 0) {
            List<BarEntry> barEntries = new ArrayList<>();
            int index = 0;
            if (workout.intervalSetIncludesPauses) {
                long time = 0;
                long lastTime = samples.get(0).absoluteTime;
                for (WorkoutSample sample : samples) {
                    if (index >= intervals.length) {
                        index = 0;
                    }
                    Interval currentInterval = intervals[index];
                    time += sample.absoluteTime - lastTime;
                    if (time > currentInterval.delayMillis) {
                        time = 0;
                        index++;
                        barEntries.add(new BarEntry((float) (sample.relativeTime) / 1000f / 60f, yMax));
                    }
                    lastTime = sample.absoluteTime;
                }
            } else {
                long time = 0;
                while (time < workout.duration) {
                    if (index >= intervals.length) {
                        index = 0;
                    }
                    Interval interval = intervals[index];

                    barEntries.add(new BarEntry((float) (time) / 1000f / 60f, yMax));

                    time += interval.delayMillis;
                    index++;
                }
            }

            BarDataSet barDataSet = new BarDataSet(barEntries, getString(R.string.intervalSet));
            barDataSet.setBarBorderWidth(3);
            barDataSet.setBarBorderColor(getThemePrimaryColor());
            barDataSet.setColor(getThemePrimaryColor());

            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.01f);
            barData.setDrawValues(false);

            combinedData.setData(barData);
        }

        chart.setData(combinedData);
        chart.setScaleXEnabled(diagramsInteractive);
        chart.setScaleYEnabled(false);
        chart.setDescription(description);
        if(diagramsInteractive){
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
                        map.getLayerManager().getLayers().remove(highlightingCircle);
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


        converter.afterAdd(chart);

        return chart;
    }

    private void onDiagramValueSelected(LatLong latLong) {
        Paint p= AndroidGraphicFactory.INSTANCE.createPaint();
        p.setColor(0xff693cff);
        highlightingCircle= new FixedPixelCircle(latLong, 20, p, null);
        map.addLayer(highlightingCircle);

        if(!map.getBoundingBox().contains(latLong)){
            map.getModel().mapViewPosition.animateTo(latLong);
        }
    }

    interface SampleConverter{
        void onCreate();
        float getValue(WorkoutSample sample);
        String getName();
        String getDescription();
        boolean isIntervalSetVisible();
        void afterAdd(CombinedChart chart);
    }

    private WorkoutSample findSample(Entry entry) {
        if (entry.getData() instanceof WorkoutSample) {
            return (WorkoutSample) entry.getData();
        } else {
            return null;
        }
    }

    void addHeightDiagram(){
        addDiagram(new SampleConverter() {
            @Override
            public void onCreate() { }

            @Override
            public float getValue(WorkoutSample sample) {
                return (float) distanceUnitUtils.getDistanceUnitSystem().getDistanceFromMeters(sample.elevation);
            }

            @Override
            public String getName() {
                return getString(R.string.height);
            }

            @Override
            public String getDescription() {
                return "min - " + distanceUnitUtils.getDistanceUnitSystem().getShortDistanceUnit();
            }

            @Override
            public boolean isIntervalSetVisible() {
                return false;
            }

            @Override
            public void afterAdd(CombinedChart chart) {
                heightDiagram= chart;
            }
        });
    }

    void addSpeedDiagram(){
        addDiagram(new SampleConverter() {
            @Override
            public void onCreate() {
                WorkoutManager.roundSpeedValues(samples);
            }

            @Override
            public float getValue(WorkoutSample sample) {
                return (float) distanceUnitUtils.getDistanceUnitSystem().getSpeedFromMeterPerSecond(sample.tmpRoundedSpeed);
            }

            @Override
            public String getName() {
                return getString(R.string.workoutSpeed);
            }

            @Override
            public String getDescription() {
                return "min - " + distanceUnitUtils.getDistanceUnitSystem().getSpeedUnit();
            }

            @Override
            public boolean isIntervalSetVisible() {
                return true;
            }

            @Override
            public void afterAdd(CombinedChart chart) {
                speedDiagram= chart;
            }
        });
    }

    boolean fullScreenItems = false;
    LinearLayout mapRoot;

    void addMap(){
        map= new MapView(this);
        downloadLayer = MapManager.setupMap(map);

        WorkoutLayer workoutLayer= new WorkoutLayer(samples, getThemePrimaryColor());
        map.addLayer(workoutLayer);

        final BoundingBox bounds= new BoundingBox(workoutLayer.getLatLongs()).extendMeters(50);
        mHandler.postDelayed(() -> {
            map.getModel().mapViewPosition.setMapPosition(new MapPosition(bounds.getCenterPoint(),
                    (LatLongUtils.zoomForBounds(map.getDimension(), bounds, map.getModel().displayModel.getTileSize()))));
            map.animate().alpha(1f).setDuration(1000).start();
        }, 1000);

        map.getModel().mapViewPosition.setMapLimit(bounds);

        mapRoot= new LinearLayout(this);
        mapRoot.setOrientation(LinearLayout.VERTICAL);
        mapRoot.addView(map);

        root.addView(mapRoot, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fullScreenItems ? ViewGroup.LayoutParams.MATCH_PARENT : getMapHeight()));
        map.setAlpha(0);


        Paint pGreen= AndroidGraphicFactory.INSTANCE.createPaint();
        pGreen.setColor(Color.GREEN);
        map.addLayer(new FixedPixelCircle(samples.get(0).toLatLong(), 20, pGreen, null));
        Paint pRed= AndroidGraphicFactory.INSTANCE.createPaint();
        pRed.setColor(Color.RED);

        map.addLayer(new FixedPixelCircle(samples.get(samples.size()-1).toLatLong(), 20, pRed, null));

        map.setClickable(false);

    }

    private int getMapHeight() {
        return getWindowManager().getDefaultDisplay().getWidth()*3/4;
    }

    protected boolean hasSamples() {
        return samples.size() > 1;
    }

    @Override
    protected void onDestroy() {
        if (map != null) {
            map.destroyAll();
        }
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (downloadLayer != null) {
            downloadLayer.onPause();
        }
    }

    public void onResume(){
        super.onResume();
        if (downloadLayer != null) {
            downloadLayer.onResume();
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
