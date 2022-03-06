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

package de.tadris.fitness.ui.record;

import android.Manifest;
import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.view.InputListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.R;
import de.tadris.fitness.data.GpsSample;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.map.MapManager;
import de.tadris.fitness.recording.BaseRecorderService;
import de.tadris.fitness.recording.BaseWorkoutRecorder;
import de.tadris.fitness.recording.event.LocationChangeEvent;
import de.tadris.fitness.recording.event.WorkoutGPSStateChanged;
import de.tadris.fitness.recording.gps.GpsRecorderService;
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;

public class RecordGpsWorkoutActivity extends RecordWorkoutActivity {

    public static final int REQUEST_CODE_LOCATION_PERMISSION = 10;
    public static final int REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION = 11;

    private MapView mapView;
    private Polyline polyline;
    private final List<LatLong> latLongList = new ArrayList<>();
    private TextView gpsStatusView;
    private ImageView focusGpsOverlay;
    private boolean focusGpsForce = true;
    private boolean gpsFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        boolean wasAlreadyRunning = false;
        if (!LAUNCH_ACTION.equals(intent.getAction())) {
            wasAlreadyRunning = true;
        } else if (instance.recorder != null && instance.recorder.isActive() && instance.recorder.getState() != BaseWorkoutRecorder.RecordingState.IDLE) {
            wasAlreadyRunning = true;
        }


        if (wasAlreadyRunning) {
            activity = instance.recorder.getWorkout().getWorkoutType(this);
            wasAlreadyRunning = true;
        } else {
            Serializable workoutType = intent.getSerializableExtra(WORKOUT_TYPE_EXTRA);
            if (workoutType instanceof WorkoutType) {
                activity = (WorkoutType) workoutType;
                // Create New Recorder when new is Launched

                // Save Possibly Running Recorder...
                // TODO Add Dialog, prefere Resume or Delete
                if (instance.recorder != null &&
                        instance.recorder.getState() != BaseWorkoutRecorder.RecordingState.IDLE) {
                    instance.recorder.stop("New activity will be started");
                    saveIfNotSaved();
                }
                instance.recorder = new GpsWorkoutRecorder(getApplicationContext(), activity);
            }
        }

        initBeforeContent();
        setContentView(R.layout.activity_record_workout);
        initAfterContent();

        checkPermissions();

        setTitle(R.string.recordWorkout);

        setupMap();

        ((ViewGroup) findViewById(R.id.recordMapViewerRoot)).addView(mapView);
        waitingForGPSOverlay = findViewById(R.id.recorderWaitingOverlay);
        waitingForGPSOverlay.setVisibility(View.VISIBLE);
        gpsStatusView = findViewById(R.id.recordGpsStatus);

        focusGpsOverlay = findViewById(R.id.gpsFocus);
        focusGpsOverlay.setVisibility(View.GONE);
        focusGpsOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                focusGpsForce = true;
                if (!latLongList.isEmpty()) {
                    focusGpsOverlay.setVisibility(View.GONE);
                    mapView.getModel().mapViewPosition.animateTo(latLongList.get(latLongList.size() - 1));
                }
            }
        });

        onGPSStateChanged(new WorkoutGPSStateChanged(GpsWorkoutRecorder.GpsState.SIGNAL_LOST, GpsWorkoutRecorder.GpsState.SIGNAL_LOST));

        if (wasAlreadyRunning) {
            if (instance.recorder.getState() != GpsWorkoutRecorder.RecordingState.IDLE) {
                recordStartButtonsRoot.setVisibility(View.INVISIBLE);
                timeView.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
            }

            latLongList.clear();
            List<GpsSample> samples = ((GpsWorkoutRecorder) instance.recorder).getSamples();
            for (GpsSample sample : samples) {
                latLongList.add(sample.toLatLong());
            }
            updateLine();

            GpsWorkoutRecorder.GpsState gpsState = ((GpsWorkoutRecorder) instance.recorder).getGpsState();
            onGPSStateChanged(new WorkoutGPSStateChanged(gpsState, gpsState));
        }
    }

    private void hideWaitOverlay() {
        waitingForGPSOverlay.clearAnimation();
        waitingForGPSOverlay.animate().alpha(0f).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                waitingForGPSOverlay.setVisibility(View.GONE);
            }
        }).start();
    }

    private void setupMap() {
        final boolean showZoomControls = instance.userPreferences.getShowWorkoutZoomControls();
        final boolean setClickable = showZoomControls;
        mapView = new MapManager(this).setupMap();

        mapView.setBuiltInZoomControls(showZoomControls);
        mapView.setClickable(setClickable);

        mapView.addInputListener(new InputListener() {
            @Override
            public void onMoveEvent() {
                if (!latLongList.isEmpty()) {
                    final LatLong center = mapView.getBoundingBox().getCenterPoint();
                    final LatLong latLongGps = latLongList.get(latLongList.size() - 1);
                    final double distanceMeter = Math.abs(latLongGps.sphericalDistance(center));
                    final boolean distanceThresholdExceeded = distanceMeter > 50;
                    final boolean focusGps = !distanceThresholdExceeded;

                    if (distanceThresholdExceeded) {
                        focusGpsOverlay.setVisibility(View.VISIBLE);
                        focusGpsForce = false;
                        handleLocation(latLongGps);
                    } else {
                        focusGpsOverlay.setVisibility(View.GONE);
                        focusGpsForce = true;
                    }
                }
            }

            @Override
            public void onZoomEvent() {

            }
        });
    }

    private void updateLine() {
        if (polyline != null) {
            mapView.getLayerManager().getLayers().remove(polyline);
        }
        Paint p = AndroidGraphicFactory.INSTANCE.createPaint();
        p.setColor(getThemePrimaryColor());
        p.setStrokeWidth(20);
        p.setStyle(Style.STROKE);
        polyline = new Polyline(p, AndroidGraphicFactory.INSTANCE);
        polyline.setPoints(latLongList);
        mapView.addLayer(polyline);
    }

    private void checkPermissions() {
        if (!hasPermission()) {
            showLocationPermissionConsent();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !hasBackgroundPermission()) {
            // We cannot request location permission and background permission at the same time due to android 11+ behaviour
            showBackgroundLocationPermissionConsent();
        }
    }

    private void showLocationPermissionConsent() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.recordingPermissionNotGrantedTitle)
                .setMessage(R.string.recordingPermissionNotGrantedMessage)
                .setPositiveButton(R.string.actionGrant, (dialog, which) -> requestLocationPermission())
                .setNegativeButton(R.string.cancel, (dialog, which) -> activityFinish())
                .show();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void showBackgroundLocationPermissionConsent() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.recordingPermissionNotGrantedTitle)
                .setMessage(R.string.recordingBackgroundPermissionNotGrantedMessage)
                .setPositiveButton(R.string.actionGrant, (dialog, which) -> requestBackgroundLocationPermission())
                .setNegativeButton(R.string.cancel, (dialog, which) -> activityFinish())
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION);
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (hasPermission()) {
                // Restart LocationListener so it can retry to register for location updates now that we got permission
                restartListener();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundPermission()) {
                    showBackgroundLocationPermissionConsent();
                }
            } else {
                showPermissionsNotGrantedDialog(R.string.recordingPermissionNotGrantedMessage);
            }
        } else if (requestCode == REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION && !hasBackgroundPermission()) {
            showPermissionsNotGrantedDialog(R.string.recordingBackgroundPermissionNotGrantedMessage);
        }
    }

    private void showPermissionsNotGrantedDialog(@StringRes int message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.recordingPermissionNotGrantedTitle)
                .setMessage(message)
                .setPositiveButton(R.string.settings, (dialog, which) -> openSystemSettings())
                .create().show();
    }

    @Override
    Class<? extends BaseRecorderService> getServiceClass() {
        return GpsRecorderService.class;
    }

    @Override
    protected void onListenerStart() {
        checkGpsStatus();
    }

    private void checkGpsStatus() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            openDialogNoGps();
        }
    }

    private void openDialogNoGps() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.noGpsTitle)
                .setMessage(R.string.noGpsMessage)
                .setNegativeButton(R.string.cancel, (dialog, which) -> activityFinish())
                .setPositiveButton(R.string.enable, (dialog, which) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setCancelable(false)
                .create().show();
    }

    @Subscribe
    public void onLocationChange(LocationChangeEvent e) {
        LatLong latLongGps = GpsRecorderService.locationToLatLong(e.location);
        handleLocation(latLongGps);
   }

   private void handleLocation(LatLong latLongGps){
       final LatLong center = mapView.getBoundingBox().getCenterPoint();
       final boolean focusGps = focusGpsForce || latLongList.isEmpty();

       if (focusGps) {
           mapView.getModel().mapViewPosition.animateTo(latLongGps);
       }

       if (instance.recorder.getState() == GpsWorkoutRecorder.RecordingState.RUNNING) {
           latLongList.add(latLongGps);
           updateLine();
       }

       foundGPS();

   }

    @Override
    protected void onDestroy() {
        // Clear map
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();

        super.onDestroy();
    }

    @Override
    public void onPause() {
        for (Layer layer : mapView.getLayerManager().getLayers()) {
            if (layer instanceof TileDownloadLayer) {
                ((TileDownloadLayer) layer).onPause();
            }
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        for (Layer layer : mapView.getLayerManager().getLayers()) {
            if (layer instanceof TileDownloadLayer) {
                ((TileDownloadLayer) layer).onResume();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGPSStateChanged(WorkoutGPSStateChanged e) {

        GpsWorkoutRecorder.GpsState state = e.newState;
        gpsStatusView.setTextColor(state.color);

        if (state != GpsWorkoutRecorder.GpsState.SIGNAL_LOST) {
            foundGPS();
        }

        if (instance.recorder.getState() == GpsWorkoutRecorder.RecordingState.IDLE) {
            if (state == GpsWorkoutRecorder.GpsState.SIGNAL_OKAY) {
                updateStartButton(true, R.string.start, v -> start());
            } else {
                updateStartButton(false, R.string.cannotStart, null);
            }
        }
    }

    private void foundGPS() {
        if (!gpsFound) {
            gpsFound = true;
            hideWaitOverlay();
        }
    }
}
