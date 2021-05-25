package de.tadris.fitness.recording.announcement.interval;

import de.tadris.fitness.data.Interval;
import de.tadris.fitness.recording.announcement.Announcement;
import de.tadris.fitness.recording.gps.GpsWorkoutRecorder;

public class IntervalAnnouncement implements Announcement {

    private Interval interval;

    public IntervalAnnouncement(Interval interval) {
        this.interval = interval;
    }

    @Override
    public boolean isAnnouncementEnabled() {
        return true;
    }

    @Override
    public String getSpokenText(GpsWorkoutRecorder recorder) {
        return interval.name;
    }

}
