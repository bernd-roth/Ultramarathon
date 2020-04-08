package de.tadris.fitness.recording.information;

import android.content.Context;

import de.tadris.fitness.R;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.util.unit.UnitUtils;

public class CurrentSpeed extends WorkoutInformation {

    public CurrentSpeed(Context context) {
        super(context);
    }

    @Override
    public String getId() {
        return "current_speed";
    }

    @Override
    boolean isEnabledByDefault() {
        return true;
    }

    @Override
    boolean canBeDisplayed() {
        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.currentSpeed);
    }

    @Override
    String getDisplayedText(WorkoutRecorder recorder) {
        return UnitUtils.getSpeed(recorder.getCurrentSpeed());
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        return getString(R.string.currentSpeed) + ": " + UnitUtils.getSpeed(recorder.getCurrentSpeed()) + ".";
    }
}
