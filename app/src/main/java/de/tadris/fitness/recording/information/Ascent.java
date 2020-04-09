package de.tadris.fitness.recording.information;

import android.content.Context;

import de.tadris.fitness.R;
import de.tadris.fitness.recording.WorkoutRecorder;

public class Ascent extends WorkoutInformation {

    public Ascent(Context context) {
        super(context);
    }

    @Override
    public String getId() {
        return "ascent";
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
        return getString(R.string.workoutAscent);
    }

    @Override
    String getDisplayedText(WorkoutRecorder recorder) {
        return getDistanceUnitUtils().getDistance(recorder.getAscent());
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        return getString(R.string.workoutAscent) + ": " + getDistanceUnitUtils().getDistance(recorder.getAscent(), true) + ".";
    }
}
