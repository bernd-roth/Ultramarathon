package de.tadris.fitness.recording.autostart;

import android.content.Context;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.recording.WorkoutRecorder;

/**
 * This class provides countdown time announcements for even minutes, i.e. the amount of minutes
 * left plus some more words will be spoken.
 *
 * @apiNote Use this announcement type for even minutes, e.g. 10m0s or 8m0s.<p>
 *     Do NOT use it for 1m0s, however, as the plural form of minutes is used and the announcement
 *     might thus be grammatically incorrect depending on the language.
 * @see MinuteSecondCountdownTimeAnnouncement
 */
public class MinuteCountdownTimeAnnouncement extends CountdownTimeAnnouncement {
    private final Context context;

    public MinuteCountdownTimeAnnouncement(Context context, Instance instance, int countdownS) {
        super(instance, countdownS);
        this.context = context;
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        int minutes = getCountdownS() / 60;
        String mins = context.getResources().getQuantityString(R.plurals.minutes, minutes, minutes);
        return context.getString(R.string.ttsMinuteCountdownAnnouncement, mins);
    }
}
