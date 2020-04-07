package de.tadris.fitness.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;

import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.recording.information.InformationManager;
import de.tadris.fitness.recording.information.WorkoutInformation;

public class SelectWorkoutInformationDialog {

    private Activity context;
    private WorkoutInformationSelectListener listener;
    private int slot;
    private List<WorkoutInformation> informationList;

    public SelectWorkoutInformationDialog(Activity context, int slot, WorkoutInformationSelectListener listener) {
        this.context = context;
        this.listener = listener;
        this.slot = slot;
        this.informationList = new InformationManager(context).getDisplayableInformation();
    }

    public void show() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.select_dialog_singlechoice_material);
        for (WorkoutInformation information : informationList) {
            arrayAdapter.add(information.getTitle());
        }

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> onSelect(which));
        builderSingle.show();
    }

    private void onSelect(int which) {
        WorkoutInformation information = informationList.get(which);
        Instance.getInstance(context).userPreferences.setIdOfDisplayedInformation(slot, information.getId());
        listener.onSelectWorkoutInformation(slot, information);
    }

    public interface WorkoutInformationSelectListener {
        void onSelectWorkoutInformation(int slot, WorkoutInformation information);
    }

}
