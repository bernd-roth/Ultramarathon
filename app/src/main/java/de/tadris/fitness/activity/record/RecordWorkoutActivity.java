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

package de.tadris.fitness.activity.record;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.overlay.Polyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.activity.FitoTrackActivity;
import de.tadris.fitness.data.Interval;
import de.tadris.fitness.data.IntervalSet;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.dialog.SelectIntervalSetDialog;
import de.tadris.fitness.dialog.SelectWorkoutInformationDialog;
import de.tadris.fitness.map.MapManager;
import de.tadris.fitness.recording.LocationListener;
import de.tadris.fitness.recording.PressureService;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.TTSController;
import de.tadris.fitness.recording.announcement.VoiceAnnouncements;
import de.tadris.fitness.recording.information.GPSStatus;
import de.tadris.fitness.recording.information.InformationDisplay;
import de.tadris.fitness.recording.information.WorkoutInformation;
import de.tadris.fitness.util.unit.UnitUtils;

public class RecordWorkoutActivity extends FitoTrackActivity implements LocationListener.LocationChangeListener,
        WorkoutRecorder.WorkoutRecorderListener, TTSController.VoiceAnnouncementCallback,
        SelectIntervalSetDialog.IntervalSetSelectListener, InfoViewHolder.InfoViewClickListener,
        SelectWorkoutInformationDialog.WorkoutInformationSelectListener {

    public static WorkoutType ACTIVITY = WorkoutType.OTHER;

    private MapView mapView;
    private TileDownloadLayer downloadLayer;
    private WorkoutRecorder recorder;
    private Polyline polyline;
    private final List<LatLong> latLongList = new ArrayList<>();
    private final InfoViewHolder[] infoViews = new InfoViewHolder[4];
    private TextView timeView;
    private TextView gpsStatusView;
    private TextView attribution;
    private View waitingForGPSOverlay;
    private Button startButton;
    private boolean gpsFound = false;
    private boolean isResumed = false;
    private final Handler mHandler = new Handler();
    private PowerManager.WakeLock wakeLock;
    private Intent locationListener;
    private Intent pressureService;
    private boolean saved= false;
    private InformationDisplay informationDisplay;

    private boolean voiceFeedbackAvailable = false;
    private TTSController TTSController;
    private VoiceAnnouncements announcements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Instance.getInstance(this).themes.getWorkoutTypeTheme(ACTIVITY));
        setContentView(R.layout.activity_record_workout);

        setTitle(R.string.recordWorkout);

        setupMap();

        ((ViewGroup) findViewById(R.id.recordMapViewerRoot)).addView(mapView);
        waitingForGPSOverlay= findViewById(R.id.recorderWaitingOverlay);
        waitingForGPSOverlay.setVisibility(View.VISIBLE);

        attribution = findViewById(R.id.recordMapAttribution);
        startButton = findViewById(R.id.recordStart);
        startButton.setEnabled(false);
        startButton.setOnClickListener(v -> {
            hideStartButton();
            start();
        });

        checkPermissions();

        recorder= new WorkoutRecorder(this, ACTIVITY, this);

        TTSController = new TTSController(this, this);
        announcements = new VoiceAnnouncements(this, recorder, TTSController, new ArrayList<>());
        informationDisplay = new InformationDisplay(this);

        infoViews[0] = new InfoViewHolder(0, this, findViewById(R.id.recordInfo1Title), findViewById(R.id.recordInfo1Value));
        infoViews[1] = new InfoViewHolder(1, this, findViewById(R.id.recordInfo2Title), findViewById(R.id.recordInfo2Value));
        infoViews[2] = new InfoViewHolder(2, this, findViewById(R.id.recordInfo3Title), findViewById(R.id.recordInfo3Value));
        infoViews[3] = new InfoViewHolder(3, this, findViewById(R.id.recordInfo4Title), findViewById(R.id.recordInfo4Value));
        timeView= findViewById(R.id.recordTime);
        gpsStatusView= findViewById(R.id.recordGpsStatus);

        updateDescription();

        startUpdater();
        acquireWakelock();

        Instance.getInstance(this).locationChangeListeners.add(this);

        startListener();

        onGPSStateChanged(WorkoutRecorder.GpsState.SIGNAL_LOST, WorkoutRecorder.GpsState.SIGNAL_LOST);

    }

    private void acquireWakelock(){
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "de.tadris.fitotrack:workout_recorder");
        wakeLock.acquire(1000*60*120);
    }

    private void hideWaitOverlay(){
        waitingForGPSOverlay.clearAnimation();
        waitingForGPSOverlay.animate().alpha(0f).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) { }

            @Override public void onAnimationCancel(Animator animator) { }

            @Override public void onAnimationRepeat(Animator animator) { }

            @Override
            public void onAnimationEnd(Animator animator) {
                waitingForGPSOverlay.setVisibility(View.GONE);
            }
        }).start();
        hideOSMAttribution();
    }

    private void hideOSMAttribution() {
        attribution.animate().alpha(0f).setDuration(1000).setStartDelay(5000).start();
    }

    private void setupMap(){
        this.mapView= new MapView(this);
        downloadLayer = MapManager.setupMap(mapView);
    }

    private void updateLine(){
        if(polyline != null){
            mapView.getLayerManager().getLayers().remove(polyline);
        }
        Paint p= AndroidGraphicFactory.INSTANCE.createPaint();
        p.setColor(getThemePrimaryColor());
        p.setStrokeWidth(20);
        p.setStyle(Style.STROKE);
        polyline= new Polyline(p, AndroidGraphicFactory.INSTANCE);
        polyline.setPoints(latLongList);
        mapView.addLayer(polyline);
    }

    private void startUpdater(){
        new Thread(() -> {
            try{
                while (recorder.isActive()){
                    Thread.sleep(1000);
                    mHandler.post(this::updateDescription);
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }).start();
    }


    private void updateDescription() {
        if (isResumed) {
            timeView.setText(UnitUtils.getHourMinuteSecondTime(recorder.getDuration()));
            for (int i = 0; i < 4; i++) {
                updateSlot(i);
            }
        }

        announcements.check();
    }

    private void updateSlot(int slot) {
        InformationDisplay.DisplaySlot data = informationDisplay.getDisplaySlot(recorder, slot);
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
        recorder.start();
        invalidateOptionsMenu();
    }

    private void stop(){
        recorder.stop();
        if(recorder.getSampleCount() > 3){
            showEnterDescriptionDialog();
        }else{
            finish();
        }
    }

    private void saveAndClose(){
        save();
        finish();
    }

    private void save(){
        if(recorder.getSampleCount() > 3){
            recorder.save();
            saved= true;
        }
    }

    private void saveIfNotSaved(){
        if(!saved){
            save();
        }
    }

    private void showEnterDescriptionDialog(){
        final EditText editText= new EditText(this);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        requestKeyboard(editText);
        new AlertDialog.Builder(this).setTitle(R.string.enterComment).setPositiveButton(R.string.okay, (dialog, which) -> {
            dialog.dismiss();
            recorder.setComment(editText.getText().toString());
            saveAndClose();
        }).setView(editText).setOnCancelListener(dialog -> saveAndClose()).create().show();
    }

    private void showAreYouSureToStopDialog(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.stopRecordingQuestion)
                .setMessage(R.string.stopRecordingQuestionMessage)
                .setPositiveButton(R.string.stop, (dialog, which) -> stop())
                .setNegativeButton(R.string.continue_, null)
                .create().show();
    }

    private void checkPermissions() {
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
        }
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (hasPermission()) {
            startListener();
        }
    }

    private void stopListener() {
        stopService(locationListener);
        stopService(pressureService);
    }

    private void startListener() {
        if(locationListener == null){
            locationListener= new Intent(this, LocationListener.class);
            pressureService= new Intent(this, PressureService.class);
        }else{
            stopListener();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(locationListener);
            startService(pressureService);
        }else{
            startService(locationListener);
            startService(pressureService);
        }
        checkGpsStatus();
    }

    private void checkGpsStatus(){
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            openDialogNoGps();
        }
    }

    private void openDialogNoGps(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.noGpsTitle)
                .setMessage(R.string.noGpsMessage)
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                .setPositiveButton(R.string.enable, (dialog, which) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setCancelable(false)
                .create().show();
    }

    @Override
    public void onLocationChange(Location location) {
        LatLong latLong= LocationListener.locationToLatLong(location);
        mapView.getModel().mapViewPosition.animateTo(latLong);

        if (recorder.getState() == WorkoutRecorder.RecordingState.RUNNING) {
            latLongList.add(latLong);
            updateLine();
        }

        foundGPS();
    }

    @Override
    protected void onDestroy() {
        recorder.stop();
        saveIfNotSaved(); // Important to save

        // Clear map
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();

        // Shutdown TTS
        TTSController.destroy();

        super.onDestroy();
        if(wakeLock.isHeld()){
            wakeLock.release();
        }
        Instance.getInstance(this).locationChangeListeners.remove(this);
        stopListener();
    }

    @Override
    public void onPause(){
        super.onPause();
        downloadLayer.onPause();
        isResumed= false;
    }

    public void onResume(){
        super.onResume();
        enableLockScreenVisibility();
        downloadLayer.onResume();
        isResumed= true;
    }

    private void enableLockScreenVisibility() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
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
                stop();
                return true;
            case R.id.actionSelectIntervalSet:
                showIntervalSelection();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean preparationPhase = recorder.getState() == WorkoutRecorder.RecordingState.IDLE;
        menu.findItem(R.id.actionSelectIntervalSet).setVisible(preparationPhase && voiceFeedbackAvailable);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(recorder.getSampleCount() > 3){
            showAreYouSureToStopDialog();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onGPSStateChanged(WorkoutRecorder.GpsState oldState, WorkoutRecorder.GpsState state) {
        mHandler.post(() -> {
            gpsStatusView.setTextColor(state.color);

            if (state != WorkoutRecorder.GpsState.SIGNAL_LOST) {
                foundGPS();
            }

            if (recorder.getState() == WorkoutRecorder.RecordingState.IDLE) {
                if (state == WorkoutRecorder.GpsState.SIGNAL_OKAY) {
                    startButton.setText(R.string.start);
                    startButton.setEnabled(true);
                } else {
                    startButton.setText(R.string.cannotStart);
                    startButton.setEnabled(false);
                }
            }

            GPSStatus announcement = new GPSStatus(RecordWorkoutActivity.this);
            if (recorder.isResumed() && announcement.isAnnouncementEnabled()) {
                if (oldState == WorkoutRecorder.GpsState.SIGNAL_LOST) { // GPS Signal found
                    TTSController.speak(announcement.getSpokenGPSFound());
                } else if (state == WorkoutRecorder.GpsState.SIGNAL_LOST) {
                    TTSController.speak(announcement.getSpokenGPSLost());
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
        Interval[] intervals = Instance.getInstance(this).db.intervalDao().getAllIntervalsOfSet(set.id);
        List<Interval> intervalList = new ArrayList<>(Arrays.asList(intervals));
        announcements.applyIntervals(intervalList);
        recorder.setUsedIntervalSet(set);
        Toast.makeText(this, R.string.intervalSetSelected, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onVoiceAnnouncementIsReady(boolean available) {
        this.voiceFeedbackAvailable = available;
        invalidateOptionsMenu();
    }

    @Override
    public void onInfoViewClick(int slot) {
        new SelectWorkoutInformationDialog(this, slot, this).show();
    }

    @Override
    public void onAutoStop() {
        finish();
    }

    @Override
    public void onSelectWorkoutInformation(int slot, WorkoutInformation information) {
        updateDescription();
    }
}
