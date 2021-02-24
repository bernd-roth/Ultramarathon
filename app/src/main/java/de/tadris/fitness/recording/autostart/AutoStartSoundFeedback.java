package de.tadris.fitness.recording.autostart;

import android.media.ToneGenerator;
import android.util.Log;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.tadris.fitness.model.AutoStartWorkout;
import de.tadris.fitness.util.ToneGeneratorController;
import de.tadris.fitness.util.event.EventBusHelper;
import de.tadris.fitness.util.event.EventBusMember;

// TODO: switch to SoundPool maybe, it might be more stable than ToneGenerator
//  (https://stackoverflow.com/questions/13439051/tonegenerator-crash-android)

/**
 * This class automatically plays different sounds during auto start countdown so the user knows
 * how long until the workout starts and if started at all.
 */
public class AutoStartSoundFeedback implements EventBusMember {
    private static final String TAG = "AutoStartSoundFeedback";

    private EventBus eventBus;
    private ToneGeneratorController toneGeneratorController;

    public AutoStartSoundFeedback(ToneGeneratorController toneGeneratorController) {
        this.toneGeneratorController = toneGeneratorController;
    }

    @Override
    public boolean registerTo(@NonNull EventBus eventBus) {
        unregisterFromBus();
        if (!EventBusHelper.saveRegisterTo(eventBus, this)) {
            return false;
        }
        this.eventBus = eventBus;
        return true;
    }

    @Override
    public void unregisterFromBus() {
        EventBusHelper.saveUnregisterFrom(eventBus, this);
        eventBus = null;
    }

    /**
     * Automatically play sounds on certain auto start countdown values.
     * Those sounds are played using the current notification volume.
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAutoStartCountdownChange(AutoStartWorkout.CountdownChangeEvent event) {
        Log.d(TAG, "onAutoStartCountdownChange: countdown changed");
        if (0 < event.countdownS && event.countdownS <= 10) {
            toneGeneratorController.playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350);
        }
    }

    /**
     * Automatically play sounds when certain auto start countdown states are entered.
     * Those sounds are played using the current notification volume.
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAutoStartStateChange(AutoStartWorkout.StateChangeEvent event) {
        if (event.newState != event.oldState && event.newState == AutoStartWorkout.State.AUTO_START_REQUESTED) {
            toneGeneratorController.playTone(ToneGenerator.TONE_DTMF_0, 1000);
        }
    }
}
