package de.tadris.fitness.recording.information;

import android.content.Context;

import de.tadris.fitness.R;
import de.tadris.fitness.recording.WorkoutRecorder;

public class PauseDuration extends WorkoutInformation {
    public PauseDuration(Context context) {
        super(context);
    }

    @Override
    public String getId() {
        return "pause_duration";
    }

    @Override
    boolean isEnabledByDefault() {
        return false;
    }

    @Override
    boolean canBeDisplayed() {
        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.workoutPauseDuration);
    }

    @Override
    String getDisplayedText(WorkoutRecorder recorder) {
        return getDistanceUnitUtils().getHourMinuteSecondTime(recorder.getPauseDuration());
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        return null;
    }
}
