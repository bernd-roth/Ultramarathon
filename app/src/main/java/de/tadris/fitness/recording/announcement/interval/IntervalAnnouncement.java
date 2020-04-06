package de.tadris.fitness.recording.announcement.interval;

import de.tadris.fitness.data.Interval;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.Announcement;

public class IntervalAnnouncement implements Announcement {

    private Interval interval;

    public IntervalAnnouncement(Interval interval) {
        this.interval = interval;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getSpokenText(WorkoutRecorder recorder) {
        return interval.name;
    }

}
