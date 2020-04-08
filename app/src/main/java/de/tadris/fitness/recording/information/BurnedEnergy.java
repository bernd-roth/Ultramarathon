package de.tadris.fitness.recording.information;

import android.content.Context;

import de.tadris.fitness.R;
import de.tadris.fitness.recording.WorkoutRecorder;

public class BurnedEnergy extends WorkoutInformation {

    public BurnedEnergy(Context context) {
        super(context);
    }

    @Override
    public String getId() {
        return "energy_burned";
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
        return getString(R.string.workoutBurnedEnergy);
    }

    @Override
    String getDisplayedText(WorkoutRecorder recorder) {
        return recorder.getCalories() + " kcal";
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        return null;
    }
}
