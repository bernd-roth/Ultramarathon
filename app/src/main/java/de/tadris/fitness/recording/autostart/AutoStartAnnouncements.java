package de.tadris.fitness.recording.autostart;

import android.content.Context;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.model.AutoStartWorkout;
import de.tadris.fitness.recording.WorkoutRecorder;
import de.tadris.fitness.recording.announcement.TTSController;
import de.tadris.fitness.util.TelephonyHelper;
import de.tadris.fitness.util.event.EventBusHelper;
import de.tadris.fitness.util.event.EventBusMember;

/**
 * This class automatically plays voice announcements during auto start countdown so the user knows
 * how long until the workout starts and if it started at all.
 * Voice announcements have to be enabled for auto start countdown in recording settings, otherwise
 * it will not play anything at all.
 *
 * @apiNote Make sure to register to the {@link EventBus} instance on which {@link AutoStartWorkout}
 * broadcasts its events.
 * @see #registerTo(EventBus)
 * @see #unregisterFromBus()
 */
public class AutoStartAnnouncements implements EventBusMember {
    private Context context;
    private EventBus eventBus;
    private AutoStartWorkout autoStartWorkout;
    private Instance instance;
    private WorkoutRecorder recorder;
    private TTSController ttsController;
    private ArrayList<CountdownTimeAnnouncement> countdownTimeAnnouncementList = new ArrayList<>();
    private CountdownTimeAnnouncement lastSpoken;

    boolean suppressOnCall;

    public AutoStartAnnouncements(Context context, AutoStartWorkout autoStartWorkout,
                                  Instance instance, WorkoutRecorder recorder,
                                  TTSController ttsController) {
        this.context = context;
        this.autoStartWorkout = autoStartWorkout;
        this.instance = instance;
        this.recorder = recorder;
        this.ttsController = ttsController;

        suppressOnCall = instance.userPreferences.getSuppressAnnouncementsDuringCall();

        // initialize default set of announcements
        for (int i = 10; i > 0; i--) {
            countdownTimeAnnouncementList.add(new ShortSecondCountdownTimeAnnouncement(instance, i));
        }
        for (int i = 15; i <= 60; i += 5) {
            countdownTimeAnnouncementList.add(new LongSecondCountdownTimeAnnouncement(context, instance,  i));
        }
        for (int i = 75; i <= 600; i += 15) {
            // TODO fix grammatically wrong announcement for 1mXXs. The resource string uses minutes (plural).
            if (i % 60 == 0) {
                countdownTimeAnnouncementList.add(new MinuteCountdownTimeAnnouncement(context, instance,  i));
            } else {
                countdownTimeAnnouncementList.add(new MinuteSecondCountdownTimeAnnouncement(context, instance, i));
            }
        }
        for (int i = 10 * 60 + 30; i < 61 * 30; i += 30) {
            if (i % 60 == 0) {
                countdownTimeAnnouncementList.add(new MinuteCountdownTimeAnnouncement(context, instance,  i));
            } else {
                countdownTimeAnnouncementList.add(new MinuteSecondCountdownTimeAnnouncement(context, instance,  i));
            }
        }
    }

    /**
     * Check if the user's on a call currently and announcements should be suppressed.
     * @return whether announcements should be suppressed
     */
    private boolean checkCall() {
        return suppressOnCall && TelephonyHelper.isOnCall(context);
    }

    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Automatically play sounds on certain auto start countdown values.
     * Those sounds are played using the current notification volume.
     */
    @Subscribe
    public void onAutoStartCountdownChange(AutoStartWorkout.CountdownChangeEvent event) {
        // only play announcements when the user's not on a call
        if (!checkCall()) {
            // announce current countdown time
            for (CountdownTimeAnnouncement countdownTimeAnnouncement : countdownTimeAnnouncementList) {
                if (event.countdownS == countdownTimeAnnouncement.getCountdownS() &&
                        (lastSpoken == null || event.countdownS != lastSpoken.getCountdownS())) {   // prevent duplicate announcements
                    ttsController.speak(recorder, countdownTimeAnnouncement);
                    lastSpoken = countdownTimeAnnouncement;
                    break;  // there can and should only be one announcement at a time
                }
            }
        }
    }

    /**
     * Automatically play sounds when certain auto start countdown states are entered.
     * Those sounds are played using the current notification volume.
     */
    @Subscribe
    public void onAutoStartStateChange(AutoStartWorkout.StateChangeEvent event) {
        // only play announcements when the user's not on a call
        if (!checkCall()) {
            switch (event.newState) {
                case WAITING_FOR_GPS:
                    // tell the user we're waiting for more a accurate GPS position
                    ttsController.speak(recorder, new CountdownAnnouncement(instance) {
                        @Override
                        public String getSpokenText(WorkoutRecorder recorder) {
                            return context.getString(R.string.autoStartCountdownMsgGps);
                        }
                    });
                    break;
                case ABORTED_BY_USER:
                    if (event.oldState == AutoStartWorkout.State.COUNTDOWN ||
                            event.oldState == AutoStartWorkout.State.WAITING_FOR_GPS) {
                        ttsController.speak(recorder, new CountdownAnnouncement(instance) {
                            @Override
                            public String getSpokenText(WorkoutRecorder recorder) {
                                return context.getString(R.string.workoutAutoStartAborted);
                            }
                        });
                    }
                default:
                    break;
            }
        }
    }
}
