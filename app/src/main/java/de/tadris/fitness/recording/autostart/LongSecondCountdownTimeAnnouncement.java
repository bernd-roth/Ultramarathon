package de.tadris.fitness.recording.autostart;

import android.content.Context;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;

/**
 * This class provides long countdown time announcements, i.e. the amount of seconds left plus some
 * more words will be spoken.
 *
 * @apiNote Use this announcement type when there are a few seconds between announcements (e.g. ~5s).<p>
 *     Do NOT use it for one second, however, as the plural form of seconds is used and the
 *     announcement might thus be grammatically incorrect depending on the language.
 */
public class LongSecondCountdownTimeAnnouncement extends CountdownTimeAnnouncement {
    private final Context context;

    public LongSecondCountdownTimeAnnouncement(Context context, Instance instance, int countdownS) {
        super(instance, countdownS);
        this.context = context;
    }

    @Override
    public String getSpokenText(GpsWorkoutRecorder recorder) {
        int seconds = getCountdownS();
        String secs = context.getResources().getQuantityString(R.plurals.seconds, seconds, seconds);
        return context.getString(R.string.ttsLongSecondCountdownAnnouncement, secs);
    }
}
