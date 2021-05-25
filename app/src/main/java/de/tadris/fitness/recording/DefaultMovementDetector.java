package de.tadris.fitness.recording;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.recording.event.LocationChangeEvent;

/**
 * This class implements FitoTrack's default movement detector.
 *
 */
public class DefaultMovementDetector extends MovementDetector {
    private static final String TAG = "DefaultMovementDetector";

    private boolean started;
    private Context context;
    private GpsWorkout workout;
    private Location lastLocation;

    public DefaultMovementDetector(Context context, GpsWorkout workout) {
        super();
        started = false;
        this.context = context;
        this.workout = workout;
    }

    @Override
    public boolean start() {
        if (started || state != State.IDLE && state != State.STOPPED) {
            // can't start stopped detector
            return false;
        }
        state = State.STARTING;
        started = true;
        detectionState = DetectionState.NOT_SURE;
        state = State.RUNNING;
        Log.d(TAG, "start: ");
        return true;
    }

    @Override
    public boolean stop() {
        if (!started || state != State.RUNNING) {
            // can't stop unstarted detector
            return false;
        }
        state = State.STOPPING;
        started = false;
        state = State.STOPPED;
        Log.d(TAG, "stop: ");
        return true;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void onLocationChange(LocationChangeEvent e) {
        Location location = e.location;
        Log.d(TAG, "onLocationChange: " + location.toString());
        if (isStarted()) {
            Log.d(TAG, "onLocationChange: is started");
            double distance;
            if (lastLocation != null) {
                Log.d(TAG, "onLocationChange: last present");
                // Checks whether the minimum distance to last sample was reached
                // and if the time difference to the last sample is too small
                distance = Math.abs(RecorderService.locationToLatLong(lastLocation).
                        sphericalDistance(RecorderService.locationToLatLong(location)));
                long timeDiff = (location.getElapsedRealtimeNanos() -
                        lastLocation.getElapsedRealtimeNanos()) / 1_000_000L;
                if (distance < workout.getWorkoutType(context).minDistance || timeDiff < 500) {
                    Log.d(TAG, "onLocationChange: not moving");
                    detectionState = DetectionState.NOT_MOVING;
                } else {
                    Log.d(TAG, "onLocationChange: moving");
                    detectionState = DetectionState.MOVING;
                    lastLocation = location;
                }
                return;
            }
        }
        Log.d(TAG, "onLocationChange: not sure");
        detectionState = DetectionState.NOT_SURE;
        lastLocation = location;
    }
}
