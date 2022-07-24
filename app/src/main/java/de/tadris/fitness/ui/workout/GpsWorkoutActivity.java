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

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.BaseSample;
import de.tadris.fitness.data.BaseWorkout;
import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.data.GpsWorkoutData;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.preferences.UserPreferences;
import de.tadris.fitness.map.ColoringStrategy;
import de.tadris.fitness.map.GradientColoringStrategy;
import de.tadris.fitness.map.MapManager;
import de.tadris.fitness.map.MapSampleSelectionListener;
import de.tadris.fitness.map.SimpleColoringStrategy;
import de.tadris.fitness.map.WorkoutLayer;
import de.tadris.fitness.ui.workout.diagram.SpeedConverter;
import de.tadris.fitness.util.WorkoutCalculator;

public abstract class GpsWorkoutActivity extends WorkoutActivity implements MapSampleSelectionListener {

    protected GpsWorkout workout;
    protected List<GpsSample> samples;

    protected MapView mapView;
    protected WorkoutLayer workoutLayer;
    private FixedPixelCircle highlightingCircle;

    protected GpsSample selectedSample = null;

    @Override
    void initBeforeContent() {
        super.initBeforeContent();
        workout = (GpsWorkout) getWorkout();
        samples = getBaseWorkoutData().castToGpsData().getSamples();
    }

    @Override
    BaseWorkout findWorkout(long id) {
        return Instance.getInstance(this).db.gpsWorkoutDao().getWorkoutById(id);
    }

    @Override
    List<BaseSample> findSamples(long workoutId) {
        return Arrays.asList(Instance.getInstance(this).db.gpsWorkoutDao().getAllSamplesOfWorkout(workoutId));
    }

    void addMap() {
        mapView = new MapManager(this).setupMap();
        String trackStyle = Instance.getInstance(this).userPreferences.getTrackStyle();
        // emulate current behaviour


        ColoringStrategy coloringStrategy;

        // predefined set of settings that play with the colors, the mapping of the color to some
        // value and whether to blend or not. In the future it would be nice to have a nice editor
        // in the settings to tweak the numbers here and possibly create good looking colors.
        switch (trackStyle) {
            case "purple_rain":
                /* a nice set of colors generated from colorbrewer */
                coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_PURPLE, true);
                break;
            case "pink_mist":
                /* Pink is nice */
                coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_PINK, false);
                break;
            case "rainbow_warrior":
                /* Attempt to use different colors, this would be best suited for a fixed scale e.g. green is target value , red is to fast , yellow it to slow */
                coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_MAP, true);
                break;
            case "height_map":
                /* based on height map colors from green till almost black*/
                coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_HEIGHT_MAP, true);
                break;
            case "bright_night":
                coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_BRIGHT, false);
                break;
            case "mondriaan":
                coloringStrategy = GradientColoringStrategy.fromPattern(GradientColoringStrategy.PATTERN_YELLOW_RED_BLUE, false);
                break;
            default: // theme_color
                /* default: original color based on theme*/
                coloringStrategy = new SimpleColoringStrategy(getThemePrimaryColor());
                break;
        }

        workoutLayer = new WorkoutLayer(samples, new SimpleColoringStrategy(getThemePrimaryColor()), coloringStrategy);
        workoutLayer.addMapSampleSelectionListener(this);

        if (Instance.getInstance(this).userPreferences.getTrackStyleMode().equals(UserPreferences.STYLE_USAGE_ALWAYS)) {
            // Always show coloring
            workoutLayer.setSampleConverter(workout, new SpeedConverter(this));
        }

        mapView.addLayer(workoutLayer);

        final BoundingBox bounds = workoutLayer.getBoundingBox().extendMeters(50);
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

        if (showPauses) {
            Paint pBlue = AndroidGraphicFactory.INSTANCE.createPaint();
            pBlue.setColor(Color.BLUE);
            for (WorkoutCalculator.Pause pause : WorkoutCalculator.getPausesFromWorkout(getBaseWorkoutData())) {
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

    @Override
    public void onMapSelectionChanged(GpsSample sample) {
        //nada onChartSelectionChanged(sample)
    }

    @Override
    protected void onChartSelectionChanged(BaseSample sample) {
        //remove any previous layer
        if (selectedSample != null) {
            if (highlightingCircle != null) {
                mapView.getLayerManager().getLayers().remove(highlightingCircle);
            }
        }

        selectedSample = (GpsSample) sample;

        // if a sample was selected show it on the map
        if (selectedSample != null) {
            Paint p = AndroidGraphicFactory.INSTANCE.createPaint();
            p.setColor(0xff693cff);
            highlightingCircle = new FixedPixelCircle(selectedSample.toLatLong(), 10, p, null);
            mapView.addLayer(highlightingCircle);

            if (!mapView.getBoundingBox().contains(selectedSample.toLatLong())) {
                mapView.getModel().mapViewPosition.animateTo(selectedSample.toLatLong());
            }
        }
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

    protected GpsWorkoutData getGpsWorkoutData() {
        return new GpsWorkoutData(workout, samples);
    }

    @Override
    protected List<BaseSample> aggregatedSamples(long aggregationLength, StatsDataTypes.TimeSpan viewFieldSpan) {
        long endTime = samples.get(samples.size() - 1).relativeTime;
        long sampleTime = samples.get(0).relativeTime;

        LinkedList<BaseSample> aggregatedSamples = new LinkedList<>();
        LinkedList<BaseSample> viewFieldSamples = new LinkedList<>();

        // Generating a list with all samples in the current view field
        // Additionally one point before and after is added.
        boolean foundSample = false;
        for (BaseSample sample : samples) {
            if (sample instanceof GpsSample) {
                if (!viewFieldSpan.contains(sample.relativeTime) && !viewFieldSamples.isEmpty()) {
                    if (!foundSample) {
                        viewFieldSamples.removeLast();
                    } else {
                        viewFieldSamples.add(sample);
                        break;
                    }
                } else if (!foundSample && viewFieldSpan.contains(sample.relativeTime)) {
                    foundSample = true;
                }

                viewFieldSamples.add(sample);
            }
        }

        // Also add the last and the first samples to the list
        viewFieldSamples.add(samples.get(samples.size()-1));
        viewFieldSamples.add(0, samples.get(0));

        while (sampleTime < endTime) {
            GpsSample combinedSample = new GpsSample();
            ArrayList<Double> lons = new ArrayList<>();
            ArrayList<Double> lats = new ArrayList<>();

            StatsDataTypes.TimeSpan span = new StatsDataTypes.TimeSpan(sampleTime, sampleTime + aggregationLength);

            for (BaseSample sample : viewFieldSamples) {
                if (sample instanceof GpsSample) {
                    if (span.contains(sample.relativeTime)) {
                        GpsSample gpsSample = (GpsSample) sample;
                        combinedSample.speed += gpsSample.speed;
                        combinedSample.elevationMSL += gpsSample.elevationMSL;
                        combinedSample.tmpInclination += gpsSample.tmpInclination;
                        lons.add(gpsSample.lon);
                        lats.add(gpsSample.lat);
                    }
                }
            }

            long numberOfAggregations = lons.size();

            if (numberOfAggregations != 0) {
                combinedSample.speed /= numberOfAggregations;
                combinedSample.elevationMSL /= numberOfAggregations;
                combinedSample.tmpInclination /= numberOfAggregations;
                combinedSample.relativeTime = sampleTime;
                combinedSample.lon = lons.get(lons.size() / 2);
                combinedSample.lat = lats.get(lats.size() / 2);
                aggregatedSamples.add(combinedSample);
            }
            sampleTime += aggregationLength;
        }
        return aggregatedSamples;
    }
}
