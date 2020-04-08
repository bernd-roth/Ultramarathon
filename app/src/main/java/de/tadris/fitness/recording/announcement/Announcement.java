package de.tadris.fitness.recording.announcement;

import de.tadris.fitness.recording.WorkoutRecorder;

public interface Announcement {

    boolean isEnabled();

    String getSpokenText(WorkoutRecorder recorder);

}
