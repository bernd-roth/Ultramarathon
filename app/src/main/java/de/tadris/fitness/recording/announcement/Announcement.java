package de.tadris.fitness.recording.announcement;

import de.tadris.fitness.recording.WorkoutRecorder;

public interface Announcement {

    boolean isAnnouncementEnabled();

    String getSpokenText(WorkoutRecorder recorder);

}
