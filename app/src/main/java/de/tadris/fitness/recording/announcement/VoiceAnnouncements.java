package de.tadris.fitness.recording.announcement;

import android.content.Context;

import java.util.List;

import de.tadris.fitness.data.Interval;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.interval.IntervalAnnouncements;

public class VoiceAnnouncements {

    private InformationAnnouncements informationAnnouncements;
    private IntervalAnnouncements intervalAnnouncements;

    public VoiceAnnouncements(Context context, WorkoutRecorder recorder, TTSController ttsController, List<Interval> intervals) {
        this.informationAnnouncements= new InformationAnnouncements(context, recorder, ttsController);
        this.intervalAnnouncements= new IntervalAnnouncements(recorder, ttsController, intervals);
    }

    public void check(){
        intervalAnnouncements.check();
        informationAnnouncements.check();
    }

}
