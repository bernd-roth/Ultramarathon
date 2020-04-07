package de.tadris.fitness.recording.information;

import android.content.Context;

import de.tadris.fitness.Instance;
import de.tadris.fitness.data.UserPreferences;
import de.tadris.fitness.recording.WorkoutRecorder;

public class InformationDisplay {

    private UserPreferences preferences;
    private InformationManager manager;

    public InformationDisplay(Context context) {
        this.preferences = Instance.getInstance(context).userPreferences;
        this.manager = new InformationManager(context);
    }

    public DisplaySlot getDisplaySlot(WorkoutRecorder recorder, int slot) {
        String informationId = preferences.getIdOfDisplayedInformation(slot);
        WorkoutInformation information = manager.getInformationById(informationId);
        if (information != null) {
            return new DisplaySlot(slot, information.getTitle(), information.getDisplayedText(recorder));
        } else {
            return new DisplaySlot(slot, "", "");
        }
    }

    public static class DisplaySlot {

        private int slot;
        private String title;
        private String value;

        public DisplaySlot(int slot, String title, String value) {
            this.slot = slot;
            this.title = title;
            this.value = value;
        }

        public int getSlot() {
            return slot;
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }
    }

}
