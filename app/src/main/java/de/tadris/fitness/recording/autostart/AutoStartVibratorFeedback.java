package de.tadris.fitness.recording.autostart;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import de.tadris.fitness.model.AutoStartWorkout;
import de.tadris.fitness.util.VibratorController;
import de.tadris.fitness.util.event.EventBusHelper;
import de.tadris.fitness.util.event.EventBusMember;

public class AutoStartVibratorFeedback implements EventBusMember {
    private EventBus eventBus;
    private VibratorController vibratorController;

    public AutoStartVibratorFeedback(VibratorController vibratorController) {
        this.vibratorController = vibratorController;
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

    @Subscribe
    public void onAutoStartCountdownChanged(AutoStartWorkout.CountdownChangeEvent event) {
        if (0 < event.countdownS && event.countdownS <= 3) {
            vibratorController.vibrate(500);
        }
    }

    @Subscribe
    public void onAutoStartStateChanged(AutoStartWorkout.StateChangeEvent event) {
        if (event.newState != event.oldState && event.newState ==  AutoStartWorkout.State.AUTO_START_REQUESTED) {
            vibratorController.vibrate(1000);
        }
    }
}
