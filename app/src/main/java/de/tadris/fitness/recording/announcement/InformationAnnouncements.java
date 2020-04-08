package de.tadris.fitness.recording.announcement;

import android.content.Context;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.information.InformationAnnouncement;
import de.tadris.fitness.util.unit.UnitUtils;

public class InformationAnnouncements {

    private final WorkoutRecorder recorder;
    private final TTSController TTSController;
    private final AnnouncementManager manager;
    private long lastSpokenUpdateTime = 0;
    private int lastSpokenUpdateDistance = 0;

    private final long intervalTime;
    private final int intervalInMeters;

    public InformationAnnouncements(Context context, WorkoutRecorder recorder, TTSController TTSController){
        this.recorder= recorder;
        this.TTSController = TTSController;
        this.manager = new AnnouncementManager(context);

        UserPreferences prefs = Instance.getInstance(context).userPreferences;
        this.intervalTime = 60 * 1000 * prefs.getSpokenUpdateTimePeriod();
        this.intervalInMeters = (int) (1000.0 / UnitUtils.CHOSEN_SYSTEM.getDistanceFromKilometers(1) * prefs.getSpokenUpdateDistancePeriod());
    }

    public void check() {
        if (!TTSController.isTtsAvailable()) {
            return;
        } // Cannot speak

        boolean shouldSpeak = false;

        if (intervalTime != 0 && recorder.getDuration() - lastSpokenUpdateTime > intervalTime) {
            shouldSpeak = true;
        }
        if (intervalInMeters != 0 && recorder.getDistanceInMeters() - lastSpokenUpdateDistance > intervalInMeters) {
            shouldSpeak = true;
        }

        if (shouldSpeak) {
            speak();
        }
    }

    private void speak() {
        for (InformationAnnouncement announcement : manager.getAnnouncements()) {
            TTSController.speak(recorder, announcement);
        }

        lastSpokenUpdateTime = recorder.getDuration();
        lastSpokenUpdateDistance = recorder.getDistanceInMeters();
    }

}
