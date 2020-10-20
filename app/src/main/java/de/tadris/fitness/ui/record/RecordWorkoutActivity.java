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

package de.tadris.fitness.ui.record;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.overlay.Polyline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.Interval;
import de.tadris.fitness.data.IntervalSet;
import de.tadris.fitness.data.WorkoutSample;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.map.MapManager;
import de.tadris.fitness.recording.RecorderService;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.TTSController;
import de.tadris.fitness.recording.information.InformationDisplay;
import de.tadris.fitness.recording.information.RecordingInformation;
import de.tadris.fitness.recording.sensors.HeartRateMeasurement;
import de.tadris.fitness.ui.FitoTrackActivity;
import de.tadris.fitness.ui.LauncherActivity;
import de.tadris.fitness.ui.dialog.ChooseBluetoothDeviceDialog;
import de.tadris.fitness.ui.dialog.SelectIntervalSetDialog;
import de.tadris.fitness.ui.dialog.SelectWorkoutInformationDialog;
import de.tadris.fitness.util.BluetoothDevicePreferences;

public class RecordWorkoutActivity extends FitoTrackActivity implements RecorderService.RecorderServiceListener,
        WorkoutRecorder.WorkoutRecorderListener, TTSController.VoiceAnnouncementCallback,
        SelectIntervalSetDialog.IntervalSetSelectListener, InfoViewHolder.InfoViewClickListener,
        SelectWorkoutInformationDialog.WorkoutInformationSelectListener, ChooseBluetoothDeviceDialog.BluetoothDeviceSelectListener {

    public static final String LAUNCH_ACTION = "de.tadris.fitness.RecordWorkoutActivity.LAUNCH_ACTION";
    public static final String RESUME_ACTION = "de.tadris.fitness.RecordWorkoutActivity.RESUME_ACTION";
    public static final String WORKOUT_TYPE_EXTRA = "de.tadris.fitness.RecordWorkoutActivity.WORKOUT_TYPE_EXTRA";

    public static final int REQUEST_CODE_LOCATION_PERMISSION = 10;
    public static final int REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION = 11;
    public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 12;

    public WorkoutType activity = WorkoutType.OTHER;

    private MapView mapView;
    private TileDownloadLayer downloadLayer;
    private Instance instance;
    private Polyline polyline;
    private final List<LatLong> latLongList = new ArrayList<>();
    private final InfoViewHolder[] infoViews = new InfoViewHolder[4];
    private TextView timeView;
    private TextView gpsStatusView;
    private ImageView hrStatusView;
    private View waitingForGPSOverlay;
    private Button startButton;
    private boolean gpsFound = false;
    private boolean isResumed = false;
    private final Handler mHandler = new Handler();
    private PowerManager.WakeLock wakeLock;
    private InformationDisplay informationDisplay;

    private boolean voiceFeedbackAvailable = false;
    private Thread updater;
    private boolean finished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        instance = Instance.getInstance(this);
        boolean wasAlreadyRunning = false;
        if (LAUNCH_ACTION.equals(intent.getAction())) {
            Serializable workoutType = intent.getSerializableExtra(WORKOUT_TYPE_EXTRA);
            if (workoutType instanceof WorkoutType) {
                activity = (WorkoutType) workoutType;
                // Create New Recorder when new is Launched

                // Save Possibly Running Recorder...
                // TODO Add Dialog, prefere Resume or Delete
                if (instance.recorder != null &&
                        instance.recorder.getState() != WorkoutRecorder.RecordingState.IDLE) {
                    instance.recorder.stop();
                    saveIfNotSaved();
                }
                instance.recorder = new WorkoutRecorder(getApplicationContext(), activity);
            }
        } else {
            activity = instance.recorder.getWorkout().getWorkoutType();
            wasAlreadyRunning = true;
        }

        setTheme(instance.themes.getWorkoutTypeTheme(activity));
        setContentView(R.layout.activity_record_workout);

        setTitle(R.string.recordWorkout);

        setupMap();

        ((ViewGroup) findViewById(R.id.recordMapViewerRoot)).addView(mapView);
        waitingForGPSOverlay = findViewById(R.id.recorderWaitingOverlay);
        waitingForGPSOverlay.setVisibility(View.VISIBLE);

        startButton = findViewById(R.id.recordStart);
        startButton.setEnabled(false);
        startButton.setOnClickListener(v -> {
            hideStartButton();
            start();
        });

        checkPermissions();

        informationDisplay = new InformationDisplay(this);

        infoViews[0] = new InfoViewHolder(0, this, findViewById(R.id.recordInfo1Title), findViewById(R.id.recordInfo1Value));
        infoViews[1] = new InfoViewHolder(1, this, findViewById(R.id.recordInfo2Title), findViewById(R.id.recordInfo2Value));
        infoViews[2] = new InfoViewHolder(2, this, findViewById(R.id.recordInfo3Title), findViewById(R.id.recordInfo3Value));
        infoViews[3] = new InfoViewHolder(3, this, findViewById(R.id.recordInfo4Title), findViewById(R.id.recordInfo4Value));
        timeView = findViewById(R.id.recordTime);
        gpsStatusView = findViewById(R.id.recordGpsStatus);
        hrStatusView = findViewById(R.id.recordHrStatus);

        updateDescription();

        acquireWakelock();

        onGPSStateChanged(WorkoutRecorder.GpsState.SIGNAL_LOST, WorkoutRecorder.GpsState.SIGNAL_LOST);

        instance.recorderServiceListeners.add(this);
        instance.voiceAnnouncementCallbackListeners.add(this);

        startListener();

        if (wasAlreadyRunning) {
            if (instance.recorder.getState() != WorkoutRecorder.RecordingState.IDLE) {
                startButton.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu();
            }

            latLongList.clear();
            List<WorkoutSample> samples = instance.recorder.getSamples();
            for (WorkoutSample sample : samples) {
                latLongList.add(sample.toLatLong());
            }
            updateLine();

            WorkoutRecorder.GpsState gpsState = instance.recorder.getGpsState();
            onGPSStateChanged(gpsState, gpsState);
        }

        instance.recorder.addWorkoutListener(this);
    }

    private void acquireWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "de.tadris.fitotrack:workout_recorder");
        wakeLock.acquire(1000 * 60 * 120);
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
        mapView = new MapView(this);
        mapView.setClickable(false);
        downloadLayer = MapManager.setupMap(mapView);
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

    private void startUpdater() {
        if (updater == null || !updater.isAlive()) {
            updater = new Thread(() -> {
                try {
                    while (instance.recorder.isActive()) {
                        Thread.sleep(1000);
                        mHandler.post(this::updateDescription);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        if (!updater.isAlive()) {
            updater.start();
        }
    }


    private void updateDescription() {
        if (isResumed) {
            timeView.setText(instance.distanceUnitUtils.getHourMinuteSecondTime(instance.recorder.getDuration()));
            for (int i = 0; i < 4; i++) {
                updateSlot(i);
            }
        }
    }

    private void updateSlot(int slot) {
        InformationDisplay.DisplaySlot data = informationDisplay.getDisplaySlot(instance.recorder, slot);
        infoViews[slot].setText(data.getTitle(), data.getValue());
    }

    private void hideStartButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = startButton.getWidth() / 2;
            int cy = startButton.getHeight() / 2;
            float initialRadius = (float) Math.hypot(cx, cy);
            Animator anim = ViewAnimationUtils.createCircularReveal(startButton, cx, cy, initialRadius, 0f);
            anim.setDuration(500);
            anim.setInterpolator(new AccelerateInterpolator());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    startButton.setVisibility(View.INVISIBLE);
                }
            });

            anim.start();
        } else {
            startButton.animate().alpha(0f).setDuration(500).start();
        }
    }

    private void start() {
        instance.recorder.start();
        invalidateOptionsMenu();
    }

    private void stop() {
        if(instance.recorder.getState() != WorkoutRecorder.RecordingState.IDLE) { //Only Running Records can be stopped
            instance.recorder.stop();
            if (instance.recorder.getSampleCount() > 3) {
                showEnterDescriptionDialog();
            } else {
                Toast.makeText(this, R.string.workoutDiscarded, Toast.LENGTH_LONG).show();
                instance.recorder.discard();
                activityFinish();
            }
        }else{
            activityFinish();
        }
    }

    private void saveAndClose() {
        save();
        activityFinish();
    }

    private boolean save() {
        if(instance.recorder.getState() != WorkoutRecorder.RecordingState.IDLE) {
            if (instance.recorder.getSampleCount() > 3) {
                instance.recorder.save();
                return true;
            } else {
                // Inform the user about not saving the workout
                Toast.makeText(this, R.string.workoutDiscarded, Toast.LENGTH_LONG).show();
                instance.recorder.discard();
                return false;
            }
        }
        // Only Started Workouts need to be discarded
        return false;
    }

    private void saveIfNotSaved() {
        // ONLY SAVE WHEN WAS ONCE ACTIVE and Not Already Saved
        if (instance.recorder.getState() != WorkoutRecorder.RecordingState.IDLE &&
                !instance.recorder.isSaved()) {
            save();
        }
    }

    private void showEnterDescriptionDialog() {
        final EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        requestKeyboard(editText);
        new AlertDialog.Builder(this).setTitle(R.string.enterComment).setPositiveButton(R.string.okay, (dialog, which) -> {
            dialog.dismiss();
            instance.recorder.setComment(editText.getText().toString());
            saveAndClose();
        }).setView(editText).setOnCancelListener(dialog -> saveAndClose()).create().show();
    }

    private void showAreYouSureToStopDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.stopRecordingQuestion)
                .setMessage(R.string.stopRecordingQuestionMessage)
                .setPositiveButton(R.string.stop, (dialog, which) -> stop())
                .setNegativeButton(R.string.continue_, null)
                .create().show();
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
                .setMessage(R.string.recordingGrantLocationPermissionMessage)
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
                .setMessage(R.string.recordingGrantBackgroundPermissionMessage)
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
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (hasPermission()) {
                // Restart LocationListener so it can retry to register for location updates now that we got permission
                restartListener();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        !hasBackgroundPermission()) {
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
                .setPositiveButton(R.string.settings, (dialog, which) -> openLocationSettings())
                .create().show();
    }

    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private boolean isServiceRunning(Class aService) {
        final ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(aService.getName())) {
                return true;
            }
        }
        return false;
    }

    private void restartListener() {
        stopListener();
        startListener();
    }

    private void startListener() {
        if (!isServiceRunning(RecorderService.class)) {
            Intent locationListener = new Intent(getApplicationContext(), RecorderService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(locationListener);
            } else {
                startService(locationListener);
            }
        } else {
            Log.d("RecordWorkoutActivity", "Listener Already Running");
        }

        checkGpsStatus();
    }

    private void stopListener() {
        if (isServiceRunning(RecorderService.class)) {
            Intent locationListener = new Intent(getApplicationContext(), RecorderService.class);
            stopService(locationListener);
        }
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

    @Override
    public void onLocationChange(Location location) {
        LatLong latLong = RecorderService.locationToLatLong(location);
        mapView.getModel().mapViewPosition.animateTo(latLong);

        if (instance.recorder.getState() == WorkoutRecorder.RecordingState.RUNNING) {
            latLongList.add(latLong);
            updateLine();
        }

        foundGPS();
    }

    @Override
    public void onPressureChange(float pressure) {
    }

    @Override
    public void onHeartRateChange(HeartRateMeasurement measurement) {
    }

    @Override
    public void onHeartRateConnectionChange(RecorderService.HeartRateConnectionState state) {
        hrStatusView.setImageResource(state.iconRes);
        hrStatusView.setColorFilter(getResources().getColor(state.colorRes));
    }

    @Override
    protected void onDestroy() {
        // Clear map
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        instance.recorderServiceListeners.remove(this);
        instance.voiceAnnouncementCallbackListeners.remove(this);
        instance.recorder.removeWorkoutListener(this);

        // Kill Service on Finished or not Started Recording
        if (instance.recorder.getState() == WorkoutRecorder.RecordingState.STOPPED ||
                instance.recorder.getState() == WorkoutRecorder.RecordingState.IDLE) {
            //ONLY SAVE WHEN STOPPED
            saveIfNotSaved();
            stopListener();
            if (instance.recorder.getState() == WorkoutRecorder.RecordingState.IDLE) {
                // Inform the user
                Toast.makeText(this, R.string.noWorkoutStarted, Toast.LENGTH_LONG).show();
            }
        }

        super.onDestroy();
    }

    @Override
    public void onPause() {
        downloadLayer.onPause();
        isResumed = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        finished = false;
        enableLockScreenVisibility();
        invalidateOptionsMenu();
        downloadLayer.onResume();
        startUpdater();
        isResumed = true;
    }

    private void enableLockScreenVisibility() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private boolean isRestrictedInput() {
        KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode(); // return whether phone is in locked state
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.record_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRecordingStop:
                onPressStopButton();
                return true;
            case R.id.actionSelectIntervalSet:
                showIntervalSelection();
                return true;
            case R.id.actionEditHint:
                showEditInformationHint();
                return true;
            case R.id.actionConnectHR:
                chooseHRDevice();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditInformationHint() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.editDisplayedInformation)
                .setMessage(R.string.editDisplayedInformationHint)
                .setPositiveButton(R.string.okay, null)
                .show();
    }

    private void chooseHRDevice() {
        try {
            new ChooseBluetoothDeviceDialog(this, this).show();
        } catch (ChooseBluetoothDeviceDialog.BluetoothNotAvailableException ignored) {
            askToActivateBluetooth();
        }
    }

    private void askToActivateBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLUETOOTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == RESULT_OK) {
            chooseHRDevice();
        }
    }

    private void onPressStopButton() {
        if (isRestrictedInput()) {
            Toast.makeText(this, R.string.unlockPhoneStopWorkout, Toast.LENGTH_LONG).show();
        } else {
            stop();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean preparationPhase = instance.recorder.getState() == WorkoutRecorder.RecordingState.IDLE;
        menu.findItem(R.id.actionSelectIntervalSet).setVisible(preparationPhase && voiceFeedbackAvailable);
        menu.findItem(R.id.actionEditHint).setVisible(preparationPhase);
        menu.findItem(R.id.actionConnectHR).setVisible(isBluetoothSupported());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(instance.recorder.isActive() && instance.recorder.getState() != WorkoutRecorder.RecordingState.IDLE){
            // Still Running Workout
            showAreYouSureToStopDialog();
        } else {
            // Stopped or Idle Workout
            activityFinish();
            //super.onBackPressed();
        }
    }

    private synchronized void activityFinish(){
        if(!this.finished) {
            this.finished = true;
            this.finish();
            Intent launcherIntent = new Intent(this, LauncherActivity.class);
            launcherIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(launcherIntent);
        }
    }

    @Override
    public void onGPSStateChanged(WorkoutRecorder.GpsState oldState, WorkoutRecorder.GpsState state) {
        mHandler.post(() -> {
            gpsStatusView.setTextColor(state.color);

            if (state != WorkoutRecorder.GpsState.SIGNAL_LOST) {
                foundGPS();
            }

            if (instance.recorder.getState() == WorkoutRecorder.RecordingState.IDLE) {
                if (state == WorkoutRecorder.GpsState.SIGNAL_OKAY) {
                    startButton.setText(R.string.start);
                    startButton.setEnabled(true);
                } else {
                    startButton.setText(R.string.cannotStart);
                    startButton.setEnabled(false);
                }
            }
        });
    }

    private void foundGPS() {
        if (!gpsFound) {
            gpsFound = true;
            hideWaitOverlay();
        }
    }

    void showIntervalSelection() {
        new SelectIntervalSetDialog(this, this).show();
    }

    @Override
    public void onIntervalSetSelect(IntervalSet set) {
        Interval[] intervals = instance.db.intervalDao().getAllIntervalsOfSet(set.id);
        List<Interval> intervalList = new ArrayList<>(Arrays.asList(intervals));
        instance.recorder.setIntervalList(intervalList);
        instance.recorder.setUsedIntervalSet(set);
        Toast.makeText(this, R.string.intervalSetSelected, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onVoiceAnnouncementIsReady(boolean available) {
        this.voiceFeedbackAvailable = available;
        invalidateOptionsMenu();
    }

    @Override
    public void onInfoViewClick(int slot) {
        if (instance.recorder.getState() == WorkoutRecorder.RecordingState.IDLE) {
            new SelectWorkoutInformationDialog(this, slot, this).show();
        }
    }

    @Override
    public void onAutoStop() {
        activityFinish();
    }

    @Override
    public void onSelectWorkoutInformation(int slot, RecordingInformation information) {
        updateDescription();
    }

    @Override
    public void onSelectBluetoothDevice(BluetoothDevice device) {
        new BluetoothDevicePreferences(this).setAddress(BluetoothDevicePreferences.DEVICE_HEART_RATE, device.getAddress());
        restartListener();
    }

    private boolean isBluetoothSupported() {
        // Check if device has a bluetooth adapter
        return BluetoothAdapter.getDefaultAdapter() != null;
    }
}
