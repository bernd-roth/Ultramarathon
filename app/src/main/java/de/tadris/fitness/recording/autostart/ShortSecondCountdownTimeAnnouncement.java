package de.tadris.fitness.recording.autostart;

import android.content.Context;

import de.tadris.fitness.Instance;
import de.tadris.fitness.recording.WorkoutRecorder;

/**
 * This class provides short countdown time announcements, i.e. only the amount of seconds left will
 * be spoken.
 *
 * @apiNote Use this announcement type when the time between announcements is short (e.g. <2 seconds).<p>
 *     Only the plain number will be spoken, nothing else.
 */
public class ShortSecondCountdownTimeAnnouncement extends CountdownTimeAnnouncement {

    public ShortSecondCountdownTimeAnnouncement(Instance instance, int countdownS) {
        super(instance, countdownS);
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        return String.valueOf(getCountdownS());
    }
}
