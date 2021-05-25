package de.tadris.fitness.recording.autostart;

import android.content.Context;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;

/**
 * This class provides countdown time announcements for uneven minutes, i.e. the amount of minutes
 * left plus some more words will be spoken.
 *
 * @apiNote Use this announcement type to announce minutes and seconds, e.g. 10m45s or 3m12s.
 * @see MinuteCountdownTimeAnnouncement
 */
public class MinuteSecondCountdownTimeAnnouncement extends CountdownTimeAnnouncement {
    private final Context context;

    public MinuteSecondCountdownTimeAnnouncement(Context context, Instance instance, int countdownS) {
        super(instance, countdownS);
        this.context = context;
    }

    @Override
    public String getSpokenText(GpsWorkoutRecorder recorder) {
        int minutes = getCountdownS() / 60;
        int seconds = getCountdownS() - minutes * 60;
        String mins = context.getResources().getQuantityString(R.plurals.minutes, minutes, minutes);
        String secs = context.getResources().getQuantityString(R.plurals.seconds, seconds, seconds);
        return context.getString(R.string.ttsMinuteSecondCountdownAnnouncement, mins, secs);
    }
}
