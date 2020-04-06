package de.tadris.fitness.recording.announcement.interval;

import java.util.List;

import de.tadris.fitness.data.Interval;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.TTSController;

public class IntervalAnnouncements {

    private WorkoutRecorder recorder;
    private TTSController ttsController;
    private List<Interval> intervals;

    private int index= -1; // Last spoken interval
    private long speakNextAt= 0;

    public IntervalAnnouncements(WorkoutRecorder recorder, TTSController ttsController, List<Interval> intervals) {
        this.recorder = recorder;
        this.ttsController = ttsController;
        this.intervals = intervals;
    }

    public void check(){
        if(recorder.getDuration() > speakNextAt){
            speakNextInterval();
        }
    }

    private void speakNextInterval(){
        index++;
        if(index >= intervals.size()){
            index= 0;
        }
        Interval interval= intervals.get(index);
        speak(interval);
        speakNextAt+= interval.delayMillis;
    }

    private void speak(Interval interval){
        IntervalAnnouncement announcement= new IntervalAnnouncement(interval);
        ttsController.speak(recorder, announcement);
    }

}
