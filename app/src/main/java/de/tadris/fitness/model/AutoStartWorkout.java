package de.tadris.fitness.model;

import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Timer;
import java.util.TimerTask;

import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.event.WorkoutGPSStateChanged;
import de.tadris.fitness.util.event.EventBusHelper;
import de.tadris.fitness.util.event.EventBusMember;

public class AutoStartWorkout implements EventBusMember {
    public static final int DEFAULT_DELAY_S = 20;

    public enum State {
            IDLE,
            COUNTDOWN,
            WAITING_FOR_GPS,
            AUTO_START_REQUESTED,
            ABORTED_BY_USER,
            ABORTED_ALREADY_STARTED,
    }

    private static final String TAG = "AutoStartWorkoutModel";

    private State state = State.IDLE;
    private long countdownMs;
    private long lastStartCountdownMs;
    private long defaultStartCountdownMs;
    private CountDownTimer autoStartCountdownTimer;
    private final Timer autoStartOnGpsOkayTimer = new Timer("AutoStartOnGpsOkay");
    private TimerTask autoStartOnGpsOkayTask;
    private EventBus eventBus;
    private boolean gpsOkay = false;

    /**
     * Creates a AutoStartWorkout instance.
     * @param defaultStartCountdownMs the default to start the countdown with (e.g. taken from preferences)
     */
    public AutoStartWorkout(long defaultStartCountdownMs) {
        lastStartCountdownMs = defaultStartCountdownMs;
        this.defaultStartCountdownMs = defaultStartCountdownMs;
    }

    @Override
    public boolean registerTo(@NonNull EventBus eventBus) {
        unregisterFromBus();
        if(!EventBusHelper.saveRegisterTo(eventBus, this)) {
            return false;
        }
        this.eventBus = eventBus;
        return true;
    }

    @Override
    public void unregisterFromBus() {
        EventBusHelper.saveUnregisterFrom(eventBus, this);
    }

    /**
     * This event will be posted to EventBus when the internal state changes.
     */
    public static class StateChangeEvent {
        public final State oldState;
        public final State newState;
        public StateChangeEvent(State newState, State oldState) {
            this.newState = newState;
            this.oldState = oldState;
        }
    }

    /**
     * This event will be posted to EventBus when the internal countdown value changes.
     */
    public static class CountdownChangeEvent {
        public final long countdownMs;
        public final int countdownS;
        public CountdownChangeEvent(long countdownMs) {
            this.countdownMs = countdownMs;
            this.countdownS = (int) (countdownMs + 500) / 1000;
        }
    }

    /**
     * This event must be posted to EventBus to start the auto start sequence
     */
    public static class BeginEvent {
        public long countdownMs;
        public BeginEvent(long countdownMs) {
            this.countdownMs = countdownMs;
        }
    }

    /**
     * This event must be posted to EventBus to stop/abort the auto start sequence
     */
    public static class AbortEvent {
        public Reason reason;

        /**
         * Some reasons why auto start is aborted.
         */
        public enum Reason {
            /**
             * When auto start is aborted, because something else triggered recording
             */
            STARTED,
            /**
             * When the user requested to abort auto start
             */
            USER_REQ,
        }

        public AbortEvent() {
            this.reason = Reason.USER_REQ;
        }

        public AbortEvent(Reason reason) {
            this.reason = reason;
        }

    }

    /**
     * Set the internal state
     *
     * @implNote This posts a StateChangeEvent to {@link #eventBus}
     * @param newState the new state
     */
    public void setState(State newState) {
        State oldState = state;
        state = newState;
        eventBus.post(new StateChangeEvent(state, oldState));
    }

    /**
     * Get the current state
     * @return current state
     */
    public State getState() {
        return state;
    }

    /**
     * Set the current countdown value
     *
     * @implNote This posts a CountdownChangeEvent to {@link #eventBus}
     * @param newCountdownMs the new countdown value in milliseconds
     */
    public void setCountdownMs(long newCountdownMs) {
        countdownMs = newCountdownMs;
        eventBus.post(new CountdownChangeEvent(countdownMs));
    }

    /**
     * Get the current countdown value
     * @return current countdown value in milliseconds
     */
    public long getCountdownMs() {
        return countdownMs;
    }

    /**
     * Get the last initial countdown value
     * @return last initial countdown value in milliseconds
     */
    public long getLastStartCountdownMs() {
        return lastStartCountdownMs;
    }

    /**
     * Get the default countdown value
     * @return default countdown value in milliseconds
     */
    public long getDefaultStartCountdownMs() {
        return defaultStartCountdownMs;
    }

    /**
     * Start the auto start countdown
     */
    private void startCountdown() {
        if (state != State.COUNTDOWN) {
            setState(State.COUNTDOWN);
        }

        // start countdown timer
        // do this for 0s delay as well to prevent duplicate code
        autoStartCountdownTimer = new CountDownTimer(countdownMs, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "Remaining: " + millisUntilFinished);
                // (x + 500) / 1000 for rounding (otherwise the countdown would start at one
                // less than the expected value
                setCountdownMs(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                // show 0s left...
                onTick(0);

                // ...and start recording the workout
                if (gpsOkay) {
                    setState(State.AUTO_START_REQUESTED);
                    return;
                }
                // whoops, no GPS yet, wait for it before start recording
                Log.w(TAG, "Cannot start workout yet, no GPS fix or bad signal quality");

                waitForGps();
            }
        }.start();
    }

    /**
     * Wait for GPS before starting the workout automatically
     */
    private void waitForGps() {
        if (state != State.WAITING_FOR_GPS) {
            setState(State.WAITING_FOR_GPS);
        }

        // start as soon as GPS position is accurate enough
        autoStartOnGpsOkayTask = new TimerTask() {
            @Override
            public void run() {
                if (gpsOkay) {  // request auto start
                    this.cancel();  // no need to run again
                    Log.d(TAG, "GPS fix -> finally able to start workout");
                    setState(State.AUTO_START_REQUESTED);
                } else {    // continue waiting
                    Log.d(TAG, "Still no GPS fix...");
                }
            }
        };
        autoStartOnGpsOkayTimer.scheduleAtFixedRate(autoStartOnGpsOkayTask, 500, 500);
    }

    /**
     * Listen for requests to begin auto start
     */
    @Subscribe
    public void onBeginEvent(BeginEvent beginEvent) {
        // if we're already running, that should be aborted
        if (autoStartCountdownTimer != null) {
            autoStartCountdownTimer.cancel();
        }
        if (autoStartOnGpsOkayTask != null) {
            autoStartOnGpsOkayTask.cancel();
        }

        setCountdownMs(beginEvent.countdownMs);
        lastStartCountdownMs = countdownMs;

        if (countdownMs > 0) {
            startCountdown();
        } else {
            waitForGps();
        }
    }


    /**
     * Listen for requests to abort auto start
     */
    @Subscribe
    public void onAbortEvent(AbortEvent abortEvent) {
        if (autoStartCountdownTimer != null) {
            autoStartCountdownTimer.cancel();
        }
        if (autoStartOnGpsOkayTask != null) {
            autoStartOnGpsOkayTask.cancel();
        }

        if (abortEvent.reason == AbortEvent.Reason.USER_REQ) {
            setState(State.ABORTED_BY_USER);
        } else {
            setState(State.ABORTED_ALREADY_STARTED);
        }
    }

    /**
     * Listen for GPS state changes
     */
    @Subscribe
    public void onGpsStateChanged(WorkoutGPSStateChanged gpsStateChanged) {
        gpsOkay = gpsStateChanged.newState == WorkoutRecorder.GpsState.SIGNAL_OKAY;
    }
}
