
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

package de.tadris.fitness.recording;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tadris.fitness.BuildConfig;
import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.activity.record.RecordWorkoutActivity;
import de.tadris.fitness.data.Interval;
import de.tadris.fitness.recording.announcement.TTSController;
import de.tadris.fitness.recording.announcement.VoiceAnnouncements;
import de.tadris.fitness.recording.information.GPSStatus;
import de.tadris.fitness.util.NotificationHelper;

public class LocationListener extends Service {

    private Date serviceStartTime;

    /**
     * @param location the location whose geographical coordinates should be converted.
     * @return a new LatLong with the geographical coordinates taken from the given location.
     */
    public static LatLong locationToLatLong(Location location) {
        return new LatLong(location.getLatitude(), location.getLongitude());
    }

    private static final String TAG = "LocationListener";
    private static final int NOTIFICATION_ID = 10;

    private static final int WATCHDOG_INTERVAL = 2_500; //Triger Watchdog every 2.5 Seconds

    private LocationManager mLocationManager = null;

    private SensorManager mSensorManager = null;
    private Sensor mPressureSensor = null;
    private Instance instance = null;

    private TTSController mTTSController;
    private VoiceAnnouncements announcements;

    private WatchDogRunner mWatchdogRunner;
    private Thread mWatchdogThread = null;

    private static final int LOCATION_INTERVAL = 1000;

    private class LocationChangedListener implements android.location.LocationListener {
        final Location mLastLocation;

        LocationChangedListener(String provider) {
            Log.i(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            for (LocationChangeListener listener : instance.locationChangeListeners) {
                listener.onLocationChange(location);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged: " + provider);
        }
    }

    private class PressureListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            //Log.i(TAG, "onPressureChange: " + event.values[0]);
            instance.lastPressure = event.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class VoiceCallback implements TTSController.VoiceAnnouncementCallback {
        @Override
        public void onVoiceAnnouncementIsReady(boolean available) {
            for (TTSController.VoiceAnnouncementCallback callback : instance.voiceAnnouncementCallbackListeners) {
                callback.onVoiceAnnouncementIsReady(available);
            }
        }
    }

    private class RecordListener implements WorkoutRecorder.WorkoutRecorderListener {

        @Override
        public void onGPSStateChanged(WorkoutRecorder.GpsState oldState, WorkoutRecorder.GpsState state) {
            GPSStatus announcement = new GPSStatus(LocationListener.this);
            if (instance.recorder.isResumed() && announcement.isAnnouncementEnabled()) {
                if (oldState == WorkoutRecorder.GpsState.SIGNAL_LOST) { // GPS Signal found
                    mTTSController.speak(announcement.getSpokenGPSFound());
                } else if (state == WorkoutRecorder.GpsState.SIGNAL_LOST) {
                    mTTSController.speak(announcement.getSpokenGPSLost());
                }
            }
        }

        @Override
        public void onAutoStop() {
            stopSelf();
        }
    }

    private final PressureListener pressureListener = new PressureListener();

    private final LocationChangedListener gpsListener = new LocationChangedListener(LocationManager.GPS_PROVIDER);

    private final VoiceCallback voiceCallback = new VoiceCallback();

    private final RecordListener recordListener = new RecordListener();

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        serviceStartTime = new Date();
        Notification notification = this.getNotification();

        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    private Notification getNotification() {
        String contentText = getText(R.string.trackerRunningMessage).toString();
        if (instance.recorder.getState() != WorkoutRecorder.RecordingState.IDLE) {
            contentText = String.format(Locale.getDefault(), "\n%s\n%s: %s",
                    instance.recorder.getState().toString(),
                    getText(R.string.workoutDuration),
                    instance.distanceUnitUtils.getHourMinuteSecondTime(instance.recorder.getDuration()));
        }
        if(BuildConfig.DEBUG && serviceStartTime != null) {
            contentText = String.format("%s\n\nServiceCreateTime: %s",
                    contentText,
                    instance.userDateTimeUtils.formatTime(serviceStartTime));
        }
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getText(R.string.trackerRunning))
                .setContentText(contentText)
                .setStyle(new Notification.BigTextStyle().bigText(contentText))
                .setSmallIcon(R.drawable.notification);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationHelper.createChannels(this);
            builder.setChannelId(NotificationHelper.CHANNEL_WORKOUT);
        }

        Intent recorderActivityIntent = new Intent(this, RecordWorkoutActivity.class);
        recorderActivityIntent.setAction(RecordWorkoutActivity.RESUME_ACTION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, recorderActivityIntent, 0);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    private void updateNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, getNotification());
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        this.instance = Instance.getInstance(getBaseContext());
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, 0, gpsListener);
            checkLastKnownLocation();
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        initializePressureSensor();
        if (mSensorManager != null && mPressureSensor != null) {
            Log.i(TAG, "started Pressure Sensor");
            instance.pressureAvailable = true;
            mSensorManager.registerListener(pressureListener, mPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.i(TAG, "no Pressure Sensor Available");
            instance.pressureAvailable = false;
        }

        initializeTTS();

        initializeWatchdog();
    }

    private void checkLastKnownLocation() throws SecurityException {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            gpsListener.onLocationChanged(location);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        instance.recorder.getWorkoutRecorderListeners().remove(recordListener);

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(gpsListener);
        }

        if (mSensorManager != null && mPressureSensor != null) {
            mSensorManager.unregisterListener(pressureListener);
        }

        // Shutdown Watchdog
        mWatchdogRunner.stop();

        // Shutdown TTS
        mTTSController.destroy();

        stopForeground(true);
        super.onDestroy();
    }


    private void initializeLocationManager() {
        Log.i(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void initializePressureSensor() {
        Log.i(TAG, "initializePressureSensor");
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }
        if (mPressureSensor == null) {
            mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }
    }

    private void initializeTTS() {
        mTTSController = new TTSController(this.getApplicationContext(), voiceCallback);
        announcements = new VoiceAnnouncements(this, instance.recorder, mTTSController, new ArrayList<>());
    }

    private class WatchDogRunner implements Runnable {
        boolean running = true;

        @Override
        public void run() {
            List<Interval> lastList = null;
            running = true;
            try {
                while (running) {
                    if (!instance.recorder.getWorkoutRecorderListeners().contains(recordListener)) {
                        instance.recorder.getWorkoutRecorderListeners().add(recordListener);
                    }
                    while (instance.recorder.handleWatchdog() && running) {
                        updateNotification();
                        // UPDATE INTERVAL LIST IF NEEDED
                        List<Interval> intervalList = instance.recorder.getIntervalList();
                        if (lastList != intervalList) {
                            announcements.applyIntervals(intervalList);
                            lastList = intervalList;
                        }

                        // CHECK FOR ANNOUNCEMENTS
                        announcements.check();
                        Thread.sleep(WATCHDOG_INTERVAL);
                    }
                    Thread.sleep(WATCHDOG_INTERVAL); // Additional Retry Interval
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            running = false;
        }
    }

    private void initializeWatchdog() {
        if (mWatchdogThread == null || !mWatchdogThread.isAlive()) {
            mWatchdogRunner = new WatchDogRunner();
            mWatchdogThread = new Thread(mWatchdogRunner, "WorkoutWatchdog");
        }
        if (!mWatchdogThread.isAlive()) {
            mWatchdogThread.start();
        }
    }

    public interface LocationChangeListener {
        void onLocationChange(Location location);
    }

}
