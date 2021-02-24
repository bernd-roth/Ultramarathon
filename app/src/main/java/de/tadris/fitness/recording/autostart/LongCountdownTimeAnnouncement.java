package de.tadris.fitness.recording.autostart;

import android.content.Context;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.recording.WorkoutRecorder;

/**
 * This class provides long countdown time announcements, i.e. the amount of seconds left plus some
 * more words will be spoken.
 *
 * @apiNote Use this announcement type when there are a few seconds between announcements (e.g. ~5s).
 */
public class LongCountdownTimeAnnouncement extends CountdownTimeAnnouncement {
    private final Context context;

    public LongCountdownTimeAnnouncement(Context context, Instance instance, int countdownS) {
        super(instance, countdownS);
        this.context = context;
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        return context.getString(R.string.ttsLongCountdownAnnouncement, getCountdownS());
    }
}
